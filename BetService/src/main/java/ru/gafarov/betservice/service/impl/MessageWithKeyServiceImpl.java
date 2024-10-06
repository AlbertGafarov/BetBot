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
                pairKey = CryptoUtils.decrypt(subscribe.getSecretKey(), getSecret(user));
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
    public void putSecret(Long userId, String secret) {
        secretMap.put(userId, secret);
    }

    @Override
    public String getSecret(User user) {
        // Получить секрет из мапы ...
        return secretMap.computeIfAbsent(user.getId(), id -> {
            // ... или если в мапе нет, то получить из переписки с пользователем ...
            Optional<MessageWithKey> messageWithKeyOptional = messageWithKeyRepository.getByUserId(id);
            if (messageWithKeyOptional.isEmpty()) {
                // ... или если секрет не был создан ранее, то создать новый ...
                String autoGenerateSecretKey = UUID.randomUUID().toString().replace("-", "").substring(12);
                SecretKey.MessageWithKey messageWithKey = SecretKey.MessageWithKey.newBuilder()
                        .setSecretKey(autoGenerateSecretKey)
                        .setUser(UserConverter.toProtoUser(user))
                        .build();
                // ... И отправить его пользователю
                SecretKey.ResponseSecretKey response = grpcStub.sendAutoGenerateKeyToUser(messageWithKey);
                // ... И сохраняем номер сообщения с секретом в БД
                messageWithKeyRepository.save(MessageWithKeyConverter.toMessageWithKey(response.getMessageWithKey()));
                return autoGenerateSecretKey;
            } else {
                // Если секрет был создан ранее, получаем его из переписки по номеру сообщения из бд
                SecretKey.ResponseSecretKey message = grpcStub.getSecretMessage(SecretKey.MessageWithKey.newBuilder()
                        .setTgMessageId(messageWithKeyOptional.get().getTgMessageId())
                        .setUser(UserConverter.toProtoUser(user))
                        .build());
                return message.getMessageWithKey().getSecretKey();
            }
        }); // И добавляем полученный секрет в мапу
    }

    @Override
    public String getPairSecret(User subscriber, User subscribed) {
        Subscribe subscribe = subscribeService.getSubscribe(subscriber, subscribed);
        String subscriberSecretKey = getSecret(subscriber);
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
        Optional<MessageWithKey> messageWithKeyOptional = messageWithKeyRepository.getByUserId(protoUser.getId());
        val builder = SecretKey.ResponseSecretKey.newBuilder();
        // Если в БД есть номер сообщения с секретом, то вернуть сообщение с секретом
        if (messageWithKeyOptional.isPresent()) {
            MessageWithKey messageWithKey = messageWithKeyOptional.get();
            SecretKey.MessageWithKey messageWithKeyProto = SecretKey.MessageWithKey.newBuilder()
                    .setTgMessageId(messageWithKey.getTgMessageId())
                    // Причем вернуть секрет из мапы, а если там нет, то получить из переписки, и записать в мапу
                    .setSecretKey(getSecret(UserConverter.toUser(protoUser)))
                    .build();
            return builder.setStatus(Rs.Status.SUCCESS).setMessageWithKey(messageWithKeyProto).build();
        } else {
            return builder.setStatus(Rs.Status.NOT_FOUND).build();
        }
    }

    public String createPairSecret(String subscriberSecretKey, User subscribed) {
        String subscribedSecretKey = getSecret(subscribed);
        return Stream.of(subscriberSecretKey, subscribedSecretKey).sorted().collect(Collectors.joining());
    }

}