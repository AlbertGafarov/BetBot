package ru.gafarov.betservice.telegram.bot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.Bet;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.DeleteMessageService;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class BotServiceImpl implements BotService {

    @Lazy
    private final BetTelegramBot bot;
    private final DeleteMessageService deleteMessageService;
    private final BotMessageService botMessageService;

    @Override
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

    @Override
    public int sendTimeIsUpMessage(BetSendMessage sendMessage) {
        try {
            return bot.execute(sendMessage).getMessageId();
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
            return 0;
        }
    }

    @Override
    public int sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType) {
        return sendAndSave(sendMessage, user, botMessageType, null, null, null);
    }

    @Override
    public void sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType, boolean deleteOther) {
        if (deleteOther) {
            botMessageService.deleteByBotMessageType(user, botMessageType);
        }
        sendAndSave(sendMessage, user, botMessageType, null, null, null);
    }

    @Override
    public void sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType, DraftBet draftBet) {
        sendAndSave(sendMessage, user, botMessageType, draftBet, null, null);
    }

    @Override
    public void sendAndSaveBet(BetSendMessage sendMessage, User user, BotMessageType botMessageType, Bet bet) {
        botMessageService.deleteBotMessagesByTemplate(user, botMessageType, null, bet);
        sendAndSave(sendMessage, user, botMessageType, null, bet, null);
    }

    @Override
    public void sendAndSaveFriend(BetSendMessage sendMessage, User user, BotMessageType botMessageType, User friend) {
        botMessageService.deleteBotMessagesByTemplate(user, botMessageType, friend, null);
        sendAndSave(sendMessage, user, botMessageType, null, null, friend);
    }

    private int sendAndSave(BetSendMessage sendMessage, User user, BotMessageType botMessageType, DraftBet draftBet
            , Bet bet, User friend) {
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            sendMessage.setUser(null);
            int id = bot.execute(sendMessage).getMessageId();
            BotMessage.Builder builder = BotMessage.newBuilder().setTgMessageId(id)
                    .setType(botMessageType).setUser(user);
            if (draftBet != null) {
                builder.setDraftBet(draftBet);
            }
            if (friend != null) {
                builder.setFriend(friend);
            }
            if (bet != null) {
                builder.setBet(bet);
            }
            botMessageService.save(builder.build());

            if (sendMessage.getDelTime() > 0) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(id);
                deleteMessage.setChatId(sendMessage.getChatId());
                deleteMessageService.deleteAsync(deleteMessage, sendMessage.getDelTime());
            }
        return id;
        } catch (TelegramApiException e) {
            log.error("chatId: {}", sendMessage.getChatId());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendTimeIsUpMessage(Collection<BetSendMessage> sendMessages) {
        try {
            for (BetSendMessage sendMessage : sendMessages) {
                sendAndSave(sendMessage, sendMessage.getUser(), sendMessage.getBotMessageType());
                Thread.sleep(WAIT_NEXT_MESSAGE_MS);
            }
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
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

    @Override
    public void edit(EditMessageText editMessageText) {
        try {
            bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void edit(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            bot.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public Message forward(ForwardMessage forwardMessage) {
        try {
            return bot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public String getTextFromTgMessageById(long chatId, int tgMessageId) throws TelegramApiException {
        ForwardMessage forwardMessage = new ForwardMessage(String.valueOf(chatId), String.valueOf(chatId), tgMessageId);
        String secret;
            Message message = bot.execute(forwardMessage);
            secret = message.getText();
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), message.getMessageId());
            bot.execute(deleteMessage);

        return secret;
    }
}
