package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.ProtoBet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface ArgumentService {
    /**
     * Добавить аргумент в споре
     * **/
    ProtoBet.ResponseMessage save(ProtoBet.Argument argument) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
}
