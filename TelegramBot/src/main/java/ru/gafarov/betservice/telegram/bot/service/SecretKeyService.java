package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

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

    public SecretKey.ResponseSecretKey sendAutoGenerateKeyToUser(SecretKey.MessageWithKey messageWithKey) {
        BetSendMessage betSendMessage = new BetSendMessage(messageWithKey.getUser().getChatId());
        betSendMessage.setText(messageWithKey.getSecretKey());
        int tgMessageId = botService.sendAndSave(betSendMessage, messageWithKey.getUser(), BotMessageOuterClass.BotMessageType.SECRET_KEY);
        return SecretKey.ResponseSecretKey.newBuilder()
                .setStatusValue(1).setMessagwWithKey(messageWithKey.toBuilder().setTgMessageId(tgMessageId).build()).build();
    }
}
