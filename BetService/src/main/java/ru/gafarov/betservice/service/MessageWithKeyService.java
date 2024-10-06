package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.User;

public interface MessageWithKeyService {

    Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKey);

    String getSecret(User user);

    void putSecret(Long userId, String secret);

    String getPairSecret(User author, User receiver);

    /**
     * Метод возвращает код шифрования пользователя
     */
    SecretKey.ResponseSecretKey getSecretMessage(UserOuterClass.User user);
}
