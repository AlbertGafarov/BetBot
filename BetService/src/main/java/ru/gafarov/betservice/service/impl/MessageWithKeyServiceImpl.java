package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.SecretKeyServiceGrpc;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.converter.MessageWithKeyConverter;
import ru.gafarov.betservice.converter.UserConverter;
import ru.gafarov.betservice.entity.MessageWithKey;
import ru.gafarov.betservice.entity.Subscribe;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.repository.MessageWithKeyRepository;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.service.SubscribeService;
import ru.gafarov.betservice.service.UserService;
import ru.gafarov.betservice.utils.CryptoUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageWithKeyServiceImpl implements MessageWithKeyService {
    private final MessageWithKeyRepository messageWithKeyRepository;
    private final SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcStub;
    private final Map<Long, String> secretMap = new HashMap<>();
    private final SubscribeService subscribeService;
    private final UserService userService;

    @Override
    public Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKeyProto) {
        User user = UserConverter.toUser(messageWithKeyProto.getUser());


        // Перешифровать все парные ключи
        List<Subscribe> subscribes = subscribeService.getSubscribes(user.getId());
        for (Subscribe subscribe : subscribes) {
            String pairKey;
            if (subscribe.getSecretKey() != null) {
                String secret = getSecret(user);
                pairKey = CryptoUtils.decrypt(subscribe.getSecretKey(), secret);
            } else {
                // Или создать новые парные ключи
                pairKey = createPairSecret(messageWithKeyProto.getSecretKey(), userService.getUser(subscribe.getSubscribedId()));
            }
            // Зашифровать парный ключ новым секретом
            subscribe.setSecretKey(CryptoUtils.encrypt(pairKey, messageWithKeyProto.getSecretKey()));
            subscribeService.update(subscribe);
        }
        // Сохранить номер сообщения с ключом
        messageWithKeyRepository.save(MessageWithKeyConverter.toMessageWithKey(messageWithKeyProto));
        // Сохранить секрет в мапе с секретами
        putSecret(user.getId(), messageWithKeyProto.getSecretKey());
        return Rs.Response.newBuilder().setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public Rs.Response reSaveMessageWithKey(SecretKey.MessageWithKey messageWithKeyProto) {
        User user = UserConverter.toUser(messageWithKeyProto.getUser());
        // Попробовать расшифровать все парные ключи
        List<Subscribe> subscribes = subscribeService.getSubscribes(user.getId());
        for (Subscribe subscribe : subscribes) {
            if (subscribe.getSecretKey() != null) {
                String secret = messageWithKeyProto.getSecretKey();
                try {
                    String pairKey = CryptoUtils.decrypt(subscribe.getSecretKey(), secret);
                    Pattern pattern = Pattern.compile("[\\d\\D]{3,24}");
                    if (!pattern.matcher(pairKey).matches()) {
                        return Rs.Response.newBuilder().setStatus(Rs.Status.ERROR).build();
                    }
                } catch (RuntimeException e) {
                    return Rs.Response.newBuilder().setStatus(Rs.Status.ERROR).build();
                }
            }
        }
        // Сохранить номер сообщения с ключом
        messageWithKeyRepository.save(MessageWithKeyConverter.toMessageWithKey(messageWithKeyProto));
        // Сохранить секрет в мапе с секретами
        putSecret(user.getId(), messageWithKeyProto.getSecretKey());
        return Rs.Response.newBuilder().setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public void putSecret(Long userId, String secret) {
        secretMap.put(userId, secret);
    }

    @Override
    public String getSecret(User user) {
        log.debug("Получить секрет из мапы, chatId {}", user.getChatId());
        return secretMap.computeIfAbsent(user.getId(), id -> {
            log.info("В мапе нет секрета для chatId: {}. Будем искать в БД", user.getChatId());
            Optional<MessageWithKey> messageWithKeyOptional = messageWithKeyRepository.getByUserId(id);
            if (messageWithKeyOptional.isEmpty()) {
                log.info("Номер сообщения с секретом не найден в БД. Создаем новый секрет chatId: {}", user.getChatId());
                String autoGenerateSecretKey = UUID.randomUUID().toString().replace("-", "").substring(12);
                SecretKey.MessageWithKey messageWithKey = SecretKey.MessageWithKey.newBuilder()
                        .setSecretKey(autoGenerateSecretKey)
                        .setUser(UserConverter.toProtoUser(user))
                        .build();
                log.info("Отправляем запрос в TelegramBot для отправки сгенерированного секрета в чат, chatId: {}", user.getChatId());
                SecretKey.ResponseSecretKey response = grpcStub.sendAutoGenerateKeyToUser(messageWithKey);
                log.info("Сохраняем номер сообщения с секретом в БД chatId: {}: {}", user.getChatId(), response.getMessageWithKey().getTgMessageId());
                messageWithKeyRepository.save(MessageWithKeyConverter.toMessageWithKey(response.getMessageWithKey()));
                return autoGenerateSecretKey;
            } else {
                int tgMessageId = messageWithKeyOptional.get().getTgMessageId();
                log.debug("В БД найден номер сообщения с секретом {}, chatId: {}. Выполняем запрос в TelegramBot для получения секрета"
                        , tgMessageId, user.getChatId());
                // Если секрет был создан ранее, получаем его из переписки по номеру сообщения из бд
                SecretKey.ResponseSecretKey message = grpcStub.getSecretMessage(SecretKey.MessageWithKey.newBuilder()
                        .setTgMessageId(tgMessageId)
                        .setUser(UserConverter.toProtoUser(user))
                        .build());
                if (message.getStatus().equals(Rs.Status.SUCCESS)) {
                    log.debug("Добавляем полученный секрет в мапу chatId: {}", user.getChatId());
                    return message.getMessageWithKey().getSecretKey();
                } else {
                    log.error("В чате с пользователем chatId {} не найдено сообщение с секретом. Вероятно переписка была удалена"
                            , user.getChatId());
                    return null;
                }
            }
        });
    }

    @Override
    public String getPairSecret(User subscriber, User subscribed) {
        Subscribe subscribe = subscribeService.getSubscribe(subscriber, subscribed);
        String subscriberSecretKey = getSecret(subscriber);
        if (subscriberSecretKey == null) {
            return null;
        }
        String pairSecret;
        String secretKey = subscribe.getSecretKey();
        if (secretKey == null) {
            pairSecret = createPairSecret(subscriberSecretKey, subscribed);
            subscribe.setSecretKey(CryptoUtils.encrypt(pairSecret, subscriberSecretKey));
            subscribeService.update(subscribe);
        } else {
            pairSecret = CryptoUtils.decrypt(secretKey, subscriberSecretKey);
        }
        return pairSecret;
    }

    @Override
    public SecretKey.ResponseSecretKey getSecretMessage(UserOuterClass.User protoUser) {
        log.debug("Получаем номер сообщения с секретом из БД, chatId: {}", protoUser.getChatId());
        Optional<MessageWithKey> messageWithKeyOptional = messageWithKeyRepository.getByUserId(protoUser.getId());
        val builder = SecretKey.ResponseSecretKey.newBuilder();
        // Если в БД есть номер сообщения с секретом, то вернуть сообщение с секретом
        if (messageWithKeyOptional.isPresent()) {
            MessageWithKey messageWithKey = messageWithKeyOptional.get();
            log.debug("Номер сообщения с секретом chatId {}: {}", protoUser.getChatId(), messageWithKey.getTgMessageId());
            // Причем вернуть секрет из мапы, а если там нет, то получить из переписки, и записать в мапу
            String secret = getSecret(UserConverter.toUser(protoUser));

            val messageWithKeyProto = SecretKey.MessageWithKey.newBuilder()
                    .setTgMessageId(messageWithKey.getTgMessageId());
            if (secret != null) {
                return builder.setStatus(Rs.Status.SUCCESS)
                        .setMessageWithKey(messageWithKeyProto.setSecretKey(secret).build()).build();
            } else {
                return builder.setStatus(Rs.Status.NOT_FOUND)
                        .setMessageWithKey(messageWithKeyProto.build()).build();
            }
        } else {
            log.error("Номер сообщения с секретом chatId {}: не найден в БД", protoUser.getChatId());
            return builder.setStatus(Rs.Status.NOT_FOUND).build();
        }
    }

    public String createPairSecret(String subscriberSecretKey, User subscribed) {
        String subscribedSecretKey = getSecret(subscribed);
        return Stream.of(subscriberSecretKey, subscribedSecretKey).sorted().collect(Collectors.joining());
    }
}