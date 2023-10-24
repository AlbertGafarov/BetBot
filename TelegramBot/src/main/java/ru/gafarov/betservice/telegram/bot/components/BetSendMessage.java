package ru.gafarov.betservice.telegram.bot.components;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class BetSendMessage extends SendMessage {

    @Setter
    @Getter
    private int delTime = 0;

    public BetSendMessage(long chatId) {
        this.setChatId(chatId);
    }

    public BetSendMessage(long chatId, int delTime) {
        this.setChatId(chatId);
        this.setDelTime(delTime);
    }
}
