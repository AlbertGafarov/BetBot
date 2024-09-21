package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.betservice.entity.Subscribe;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.service.CryptoService;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.service.SubscribeService;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CryptoServiceImpl implements CryptoService {
    private final SubscribeService subscribeService;
    private final MessageWithKeyService messageWithKeyService;
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Base64.Encoder encoder = Base64.getEncoder();

    @Override
    public String encryptText(String text, User subscriber, User subscribed) {
        try {
            String secret = getSecret(subscriber, subscribed);
            return encrypt(text, secret);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decryptText(String text, User subscriber, User subscribed) {
        try {
            String secret = getSecret(subscriber, subscribed);
            return decrypt(text, secret);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSecret(User subscriber, User subscribed) throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Subscribe subscribe = subscribeService.getSubscribe(subscriber, subscribed);
        String subscriberSecretKey = messageWithKeyService.getSecret(subscriber);
        String secret;
        String secretKey = subscribe.getSecretKey();
        if (secretKey == null) {
            String subscribedSecretKey = messageWithKeyService.getSecret(subscribed);
            //Создается новый парный ключ в лексиографическом порядке

            //TODO: проблема в том, что если пользователь меняет ключ шифрования, то непонятно что делать с парным ключом
            //TODO: Надо как-то создавать парный ключ с привязкой к айди пользователей, так чтобы он был постоянным
            secret = Stream.of(subscriberSecretKey, subscribedSecretKey).sorted().collect(Collectors.joining());
            String pairSecret = encrypt(secret, subscriberSecretKey);
            subscribe.setSecretKey(pairSecret);
            subscribeService.update(subscribe);
        } else {
            secret = decrypt(secretKey, subscriberSecretKey);
        }
        return secret;
    }


    private String encrypt(String value, String secret) throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher(secret, Cipher.ENCRYPT_MODE);

        return encoder.encodeToString(cipher.doFinal(value.getBytes()));
    }

    private String decrypt(String value, String secret) throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher(secret, Cipher.DECRYPT_MODE);

        return new String(cipher.doFinal(decoder.decode(value)));
    }

    private static Cipher getCipher(String secret, int encryptMode) throws NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException {
        byte[] key = secret.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(encryptMode, secretKey);
        return cipher;
    }

}
