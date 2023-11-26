package ru.gafarov.betservice.telegram.bot.components;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;

@Setter
@Getter
public class BetSendMessage extends SendMessage {

    private int delTime = 0;
    private User user;
    private BotMessageOuterClass.BotMessageType botMessageType;

    public BetSendMessage(long chatId) {
        this.setChatId(chatId);
    }

    public BetSendMessage(long chatId, int delTime) {
        this.setChatId(chatId);
        this.setDelTime(delTime);
    }
}