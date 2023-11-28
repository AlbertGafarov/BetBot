package ru.gafarov.betservice.telegram.bot.components;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class BetEditMessageText extends EditMessageText {

    public BetEditMessageText(long chatId, int messageId) {
        this.setChatId(chatId);
        this.setMessageId(messageId);
    }
}
