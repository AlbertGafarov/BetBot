package ru.gafarov.betservice.service;

import ru.gafarov.betservice.entity.User;

public interface CryptoService {
    String encryptText(String text, User author, User receiver);

    String decryptText(String text, User author, User receiver);
}
