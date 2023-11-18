package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class DeleteMessageService {

    @Lazy
    private final BetTelegramBot bot;
    private final BotMessageService botMessageService;

    @Async("deleteMessageExecutor")
    public void deleteAsync(DeleteMessage deleteMessage, long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (botMessageService.isNotDeleted(deleteMessage.getMessageId())) {
                bot.execute(deleteMessage);
                botMessageService.markDeleted(deleteMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void deleteSync(DeleteMessage deleteMessage) {
        try {
            if (botMessageService.isNotDeleted(deleteMessage.getMessageId())) {
                bot.execute(deleteMessage);
            }
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            botMessageService.markDeleted(deleteMessage);
        }
    }

    public void deleteUserMessageSync(DeleteMessage deleteMessage) {
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
