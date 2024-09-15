package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.UserOuterClass;

public interface MessageWithKeyService {

    UserOuterClass.ResponseUser saveMessageWithKey(UserOuterClass.MessageWithKey messageWithKey);
}
