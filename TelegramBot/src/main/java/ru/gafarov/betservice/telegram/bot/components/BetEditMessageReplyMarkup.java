package ru.gafarov.betservice.telegram.bot.components;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

public class BetEditMessageReplyMarkup extends EditMessageReplyMarkup {
    public BetEditMessageReplyMarkup(long chatId, int messageId) {
        this.setChatId(chatId);
        this.setMessageId(messageId);
    }
}
