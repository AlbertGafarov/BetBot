package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.MessageWithKey;
import ru.gafarov.betservice.repository.MessageWithKeyRepository;
import ru.gafarov.betservice.service.MessageWithKeyService;

@Service
@AllArgsConstructor
public class MessageWithKeyServiceImpl implements MessageWithKeyService {
    private final MessageWithKeyRepository messageWithKeyRepository;

    @Override
    public UserOuterClass.ResponseUser saveMessageWithKey(UserOuterClass.MessageWithKey messageWithKeyProto) {
        MessageWithKey messageWithKey = new MessageWithKey();
        messageWithKey.setTgMessageId((long) messageWithKeyProto.getTgMessageId());
        messageWithKey.setUserId(messageWithKeyProto.getUser().getId());
        messageWithKeyRepository.save(messageWithKey);
        return UserOuterClass.ResponseUser.newBuilder().setStatus(Rs.Status.SUCCESS).setUser(messageWithKeyProto.getUser()).build();
    }
}
