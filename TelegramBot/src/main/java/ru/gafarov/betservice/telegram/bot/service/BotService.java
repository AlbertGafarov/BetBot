package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class BotService {

    @Lazy
    private final BetTelegramBot bot;
    private final BotMessageService botMessageService;

    @Async("deleteMessageExecutor")
    public void deleteAsync(DeleteMessage deleteMessage, long sleep) {
        try {
            log.info("запущен таймер");
            Thread.sleep(sleep);
            log.info("завершен таймер");
            if (!botMessageService.isDeleted(deleteMessage.getMessageId())) {
                bot.execute(deleteMessage);
                botMessageService.markDeleted(deleteMessage);
            }
        } catch (TelegramApiException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public int sendAndDelete(BetSendMessage sendMessage) {
        int id = 0;
        try {
            id = bot.execute(sendMessage).getMessageId();
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
        if (sendMessage.getDelTime() > 0) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(id);
            deleteMessage.setChatId(sendMessage.getChatId());
            deleteAsync(deleteMessage, sendMessage.getDelTime());
        }
        return id;
    }

    public int send(BetSendMessage sendMessage) {
        try {
            return bot.execute(sendMessage).getMessageId();
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
            return 0;
        }
    }

    public void delete(Update update) {
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
        botMessageService.markDeleted(deleteMessage);
    }

    public void edit(EditMessageText editMessageText) {
        try {
            bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void edit(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            bot.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
