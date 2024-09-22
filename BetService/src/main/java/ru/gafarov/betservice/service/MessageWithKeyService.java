package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.betservice.entity.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MessageWithKeyService {

    Rs.Response saveMessageWithKey(SecretKey.MessageWithKey messageWithKey);

    String getSecret(User user);
    void putSecret(Long userId, String secret);

    String getPairSecret(User author, User receiver)throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
}
