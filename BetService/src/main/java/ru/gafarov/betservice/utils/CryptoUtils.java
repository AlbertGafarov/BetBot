package ru.gafarov.betservice.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

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

@UtilityClass
public class CryptoUtils {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Base64.Encoder encoder = Base64.getEncoder();


    public String encryptText(String text, String pairSecret) {
        return encrypt(text, pairSecret);

    }

    public String decryptText(String text, String pairSecret) {
        return decrypt(text, pairSecret);

    }


    public String encrypt(@NonNull String value, @NonNull String secret) {
        try {
            Cipher cipher = getCipher(secret, Cipher.ENCRYPT_MODE);
            return encoder.encodeToString(cipher.doFinal(value.getBytes()));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(@NonNull String value, @NonNull String secret) {
        try {
            Cipher cipher = getCipher(secret, Cipher.DECRYPT_MODE);
            return new String(cipher.doFinal(decoder.decode(value)));
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
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
