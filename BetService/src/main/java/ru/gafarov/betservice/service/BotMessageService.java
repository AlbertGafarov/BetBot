package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;

public interface BotMessageService {
    Proto.ResponseMessage save(Proto.BotMessage request);

    Proto.ResponseBotMessage get(Proto.BotMessage request);

    Proto.ResponseBotMessage getAll(Proto.DraftBet request);

    Proto.ResponseBotMessage deleteAll(Proto.BotMessages request);

    Proto.ResponseBotMessage delete(Proto.BotMessage botMessage);

    Proto.ResponseBotMessage getWithout(Proto.DraftBet draftBet);
}
