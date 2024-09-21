package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.betservice.entity.User;

public interface MessageWithKeyService {

    Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKey);

    String getSecret(User user);
    void addSecret(Long userId, String secret);
}
