package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.bet.grpcInterface.Proto.BotMessage;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.DraftBet;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class BotService {

    @Lazy
    private final BetTelegramBot bot;
    private final DeleteMessageService deleteMessageService;
    private final BotMessageService botMessageService;

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
            deleteMessageService.deleteAsync(deleteMessage, sendMessage.getDelTime());
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

    public void sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType) {
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            int id = bot.execute(sendMessage).getMessageId();
            botMessageService.save(BotMessage.newBuilder().setTgMessageId(id)
                    .setType(botMessageType).setUser(user).build());
            if (sendMessage.getDelTime() > 0) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(id);
                deleteMessage.setChatId(sendMessage.getChatId());
                deleteMessageService.deleteAsync(deleteMessage, sendMessage.getDelTime());
            }
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType, DraftBet draftBet) {
        try {
            int id = bot.execute(sendMessage).getMessageId();
            botMessageService.save(BotMessage.newBuilder().setTgMessageId(id)
                    .setType(botMessageType).setUser(user).setDraftBet(draftBet).build());
            if (sendMessage.getDelTime() > 0) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(id);
                deleteMessage.setChatId(sendMessage.getChatId());
                deleteMessageService.deleteAsync(deleteMessage, sendMessage.getDelTime());
            }
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void delete(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        if (update.hasMessage()) {
            deleteMessage.setChatId(update.getMessage().getChatId());
            deleteMessage.setMessageId(update.getMessage().getMessageId());
            if (update.getMessage().getFrom().getIsBot()) {
                deleteMessageService.deleteSync(deleteMessage);
            } else {
                deleteMessageService.deleteUserMessageSync(deleteMessage);
            }
        } else if (update.hasCallbackQuery()) {
            deleteMessage.setChatId(update.getCallbackQuery().getFrom().getId());
            deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            deleteMessageService.deleteSync(deleteMessage);
        }
    }

    public void edit(EditMessageText editMessageText) {
        try {
            bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void edit(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            bot.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
