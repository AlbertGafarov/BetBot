package ru.gafarov.betservice.telegram.bot.service;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;

public interface BotMessageService {
    void save(BotMessageOuterClass.BotMessage botMessage);

    Integer getId(BotMessageOuterClass.BotMessage botMessage);

    void deleteByDraft(DrBet.DraftBet draftBet, UserOuterClass.User user);

    void markDeleted(DeleteMessage deleteMessage);

    boolean isNotDeleted(Integer messageId);

    void deleteWithoutDraft(DrBet.DraftBet draftBet, UserOuterClass.User user);

    void deleteByBotMessageType(UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType);

    void deleteBotMessagesByTemplate(UserOuterClass.User user, BotMessageOuterClass.BotMessageType botMessageType, UserOuterClass.User friend, ProtoBet.Bet bet);
}
