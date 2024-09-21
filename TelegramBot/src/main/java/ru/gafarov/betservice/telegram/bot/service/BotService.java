package ru.gafarov.betservice.telegram.bot.service;

import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.*;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

import java.util.Collection;

public interface BotService {
    int READ_ONE_CHAR_MS = 50;
    int WAIT_NEXT_MESSAGE_MS = 50;
    /**
     * Отправить сообщение и удалить его через указанный промежуток времени
     **/
    int sendAndDelete(BetSendMessage sendMessage);

    int sendTimeIsUpMessage(BetSendMessage sendMessage);

    /**
     * Отправить сообщение и сохранить информацию о нем в БД
     *
     * @param sendMessage    сообщение в чате
     * @param user           пользователь, которому оправлено сообщение
     * @param botMessageType тип сообщения пользователю
     **/
    void sendAndSave(BetSendMessage sendMessage, UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType);

    /**
     * Отправить сообщение и сохранить информацию о нем в БД
     *
     * @param sendMessage    сообщение в чате
     * @param user           пользователь, которому оправлено сообщение
     * @param botMessageType тип сообщения пользователю
     * @param deleteOther    признак того, что необходимо удалить из чата все сообщения этого типа
     **/
    void sendAndSave(BetSendMessage sendMessage, UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType, boolean deleteOther);

    void sendAndSave(BetSendMessage sendMessage, UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType, DrBet.DraftBet draftBet);

    void sendAndSaveBet(BetSendMessage sendMessage, UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType, ProtoBet.Bet bet);

    void sendAndSaveFriend(BetSendMessage sendMessage, UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType, UserOuterClass.User friend);


    void sendTimeIsUpMessage(Collection<BetSendMessage> sendMessages);

    void delete(Update update);

    void edit(EditMessageText editMessageText);

    void edit(EditMessageReplyMarkup editMessageReplyMarkup);

    Message forward(ForwardMessage forwardMessage);

    String getTextFromTgMessageById(long chatId, int tgMessageId);
}
