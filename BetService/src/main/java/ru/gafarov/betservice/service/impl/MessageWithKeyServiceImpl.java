package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.SecretKeyServiceGrpc;
import ru.gafarov.betservice.converter.UserConverter;
import ru.gafarov.betservice.entity.MessageWithKey;
import ru.gafarov.betservice.entity.Subscribe;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.repository.MessageWithKeyRepository;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.service.SubscribeService;
import ru.gafarov.betservice.service.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MessageWithKeyServiceImpl implements MessageWithKeyService {
    private final MessageWithKeyRepository messageWithKeyRepository;
    private final SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcStub;
    private final Map<Long, String> secretMap = new HashMap<>();
    private final SubscribeService subscribeService;
    private final UserService userService;

    @Override
    public Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKeyProto) throws NoSuchPaddingException
            , IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        User user = UserConverter.toUser(messageWithKeyProto.getUser());

        // Сохранить номер сообщения с ключом
        MessageWithKey messageWithKey = new MessageWithKey();
        messageWithKey.setTgMessageId(messageWithKeyProto.getTgMessageId());
        messageWithKey.setUserId(user.getId());
        messageWithKeyRepository.save(messageWithKey);

        // Перешифровать все парные ключи
        List<Subscribe> subscribes = subscribeService.getSubscribes(user.getId());
        for (Subscribe subscribe : subscribes) {
            String pairKey;
            if (subscribe.getSecretKey()!= null) {
                pairKey = CryptoUtils.decrypt(subscribe.getSecretKey(), getSecret(user));
            } else {
                // Или создать новые парные ключи
                pairKey = createPairSecret(messageWithKeyProto.getSecretKey(), userService.getUser(subscribe.getSubscribedId()));
            }
            // Зашифровать парный ключ новым секретом
            subscribe.setSecretKey(CryptoUtils.encrypt(pairKey, messageWithKeyProto.getSecretKey()));
            subscribeService.update(subscribe);
        }
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
            Optional<MessageWithKey> messageWithKeyOptional = messageWithKeyRepository.getByUserId(user.getId());
            if (messageWithKeyOptional.isEmpty()) {
                // ... или если секрет не был создан ранее, то создать новый ...
                String autoGenerateSecretKey = UUID.randomUUID().toString().replace("-", "").substring(12);
                SecretKey.MessageWithKey messageWithKey = SecretKey.MessageWithKey.newBuilder()
                        .setSecretKey(autoGenerateSecretKey)
                        .setUser(UserConverter.toProtoUser(user))
                        .build();
                // ... И отправить его пользователю, чтобы сохранить
                //TODO: вроде все сделано осталось протетсировать
                SecretKey.ResponseSecretKey response = grpcStub.sendAutoGenerateKeyToUser(messageWithKey);
                try {
                    saveMessageWithKey(response.getMessagwWithKey());
                } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
                return autoGenerateSecretKey;
            } else {
                SecretKey.ResponseSecretKey message = grpcStub.getSecretMessage(SecretKey.MessageWithKey.newBuilder()
                        .setTgMessageId(messageWithKeyOptional.get().getTgMessageId())
                        .setUser(UserConverter.toProtoUser(user))
                        .build());
                return message.getMessagwWithKey().getSecretKey();
            }
        });
    }


    public String getPairSecret(User subscriber, User subscribed) throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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

    public String createPairSecret(String subscriberSecretKey, User subscribed) {
        String subscribedSecretKey = getSecret(subscribed);
        return Stream.of(subscriberSecretKey, subscribedSecretKey).sorted().collect(Collectors.joining());
    }

}
