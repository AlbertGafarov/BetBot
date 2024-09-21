package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.SecretKeyServiceGrpc;
import ru.gafarov.betservice.converter.UserConverter;
import ru.gafarov.betservice.entity.MessageWithKey;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.repository.MessageWithKeyRepository;
import ru.gafarov.betservice.service.MessageWithKeyService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageWithKeyServiceImpl implements MessageWithKeyService {
    private final MessageWithKeyRepository messageWithKeyRepository;
    private final SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcStub;
    private final Map<Long, String> secretMap = new HashMap<>();

    @Override
    public Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKeyProto) {
        MessageWithKey messageWithKey = new MessageWithKey();
        messageWithKey.setTgMessageId(messageWithKeyProto.getTgMessageId());
        messageWithKey.setUserId(messageWithKeyProto.getUser().getId());
        messageWithKeyRepository.save(messageWithKey);
        addSecret(messageWithKey.getUserId(), messageWithKeyProto.getSecretKey());
        return Rs.Response.newBuilder().setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public void addSecret(Long userId, String secret) {
        secretMap.put(userId, secret);
    }

    @Override
    public String getSecret(User user) {
        return secretMap.computeIfAbsent(user.getChatId(), id -> {
            MessageWithKey messageWithKey = messageWithKeyRepository.getByUserId(user.getId());
            SecretKey.ResponseSecretKey message = grpcStub.getSecretMessage(SecretKey.MessageWithKey.newBuilder()
                    .setTgMessageId(messageWithKey.getTgMessageId())
                    .setUser(UserConverter.toProtoUser(user))
                    .build());
            return message.getMessagwWithKey().getSecretKey();
        });
    }
}
