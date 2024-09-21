package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.UserOuterClass;

@Service
@RequiredArgsConstructor
public class SecretKeyService {

    private final BotService botService;

    public SecretKey.ResponseSecretKey getSecretMessage(SecretKey.MessageWithKey messageWithKey) {
        UserOuterClass.User user = messageWithKey.getUser();
        String secret = botService.getTextFromTgMessageById(user.getChatId(), messageWithKey.getTgMessageId());

        return SecretKey.ResponseSecretKey.newBuilder()
                .setMessagwWithKey(SecretKey.MessageWithKey.newBuilder()
                        .setSecretKey(secret)
                        .setUser(user)
                        .build())
                .setStatusValue(0)
                .build();
    }
}
