package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.*;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

@Service
@RequiredArgsConstructor
public class SecretKeyService {

    private final BotService botService;
    private final SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcSecretKeyStub;

    public SecretKey.ResponseSecretKey getSecretMessage(SecretKey.MessageWithKey messageWithKey) {
        UserOuterClass.User user = messageWithKey.getUser();
        String secret = botService.getTextFromTgMessageById(user.getChatId(), messageWithKey.getTgMessageId());

        return SecretKey.ResponseSecretKey.newBuilder()
                .setMessageWithKey(SecretKey.MessageWithKey.newBuilder()
                        .setSecretKey(secret).setUser(user).build())
                .setStatusValue(0)
                .build();
    }

    public SecretKey.ResponseSecretKey sendAutoGenerateKeyToUser(SecretKey.MessageWithKey messageWithKey) {
        BetSendMessage infoMessage = new BetSendMessage(messageWithKey.getUser().getChatId());
        infoMessage.setText("Для Вас сгенерирован секретный ключ. Это потребовалось сделать, т.к. Ваш оппонент использует шифрование.");
        botService.sendAndSave(infoMessage, messageWithKey.getUser(), BotMessageOuterClass.BotMessageType.SECRET_KEY_SAVED);

        BetSendMessage betSendMessage = new BetSendMessage(messageWithKey.getUser().getChatId());
        betSendMessage.setText(messageWithKey.getSecretKey());
        int tgMessageId = botService.sendAndSave(betSendMessage, messageWithKey.getUser(), BotMessageOuterClass.BotMessageType.SECRET_KEY);

        return SecretKey.ResponseSecretKey.newBuilder()
                .setStatusValue(1).setMessageWithKey(messageWithKey.toBuilder().setTgMessageId(tgMessageId).build()).build();
    }

    public SecretKey.MessageWithKey getSecretMessage(UserOuterClass.User user) {
        SecretKey.ResponseSecretKey response = grpcSecretKeyStub.hasSecretMessage(user);
        return response.getMessageWithKey();
    }
}
