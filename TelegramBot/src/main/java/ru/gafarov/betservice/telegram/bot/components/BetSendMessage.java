package ru.gafarov.betservice.telegram.bot.components;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gafarov.bet.grpcInterface.Proto.*;

@Setter
@Getter
public class BetSendMessage extends SendMessage {

    private int delTime = 0;
    private User user;
    private BotMessageType botMessageType;

    public BetSendMessage(long chatId) {
        this.setChatId(chatId);
    }

    public BetSendMessage(long chatId, int delTime) {
        this.setChatId(chatId);
        this.setDelTime(delTime);
    }
    public BetSendMessage(User user, int delTime) {
        this.setChatId(user.getChatId());
        this.setDelTime(delTime);
        this.setUser(user);
    }
    public BetSendMessage(User user) {
        this.setChatId(user.getChatId());
        this.setUser(user);
    }
}
