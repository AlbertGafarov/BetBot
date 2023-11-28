package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.ProtoBet;

public interface BotMessageService {
    ProtoBet.ResponseMessage save(BotMessageOuterClass.BotMessage request);

    BotMessageOuterClass.ResponseBotMessage get(BotMessageOuterClass.BotMessage request);

    BotMessageOuterClass.ResponseBotMessage getAll(DrBet.DraftBet request);

    BotMessageOuterClass.ResponseBotMessage deleteAll(BotMessageOuterClass.BotMessages request);

    BotMessageOuterClass.ResponseBotMessage delete(BotMessageOuterClass.BotMessage botMessage);

    BotMessageOuterClass.ResponseBotMessage getWithout(DrBet.DraftBet draftBet);

    BotMessageOuterClass.ResponseBotMessage getAllByTemplate(BotMessageOuterClass.BotMessage request);
}
