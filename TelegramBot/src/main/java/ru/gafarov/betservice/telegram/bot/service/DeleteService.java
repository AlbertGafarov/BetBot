package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class DeleteService {

    @Lazy
    private final BetTelegramBot bot;
    @Async
    public void delete(DeleteMessage deleteMessage, long sleep) {
        try {
            Thread.sleep(sleep);
            bot.execute(deleteMessage);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delete(Update update){
        long chatId = 0;
        int messageId = 0;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        }
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        bot.delete(deleteMessage);
    }
}