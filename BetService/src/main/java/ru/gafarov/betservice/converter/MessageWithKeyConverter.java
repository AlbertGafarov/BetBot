package ru.gafarov.betservice.converter;

import lombok.experimental.UtilityClass;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.betservice.entity.MessageWithKey;

@UtilityClass
public class MessageWithKeyConverter {
    public MessageWithKey toMessageWithKey(SecretKey.MessageWithKey messageWithKeyProto) {
        MessageWithKey messageWithKey = new MessageWithKey();
        messageWithKey.setTgMessageId(messageWithKeyProto.getTgMessageId());
        messageWithKey.setUserId(messageWithKeyProto.getUser().getId());
        return messageWithKey;
    }
}
