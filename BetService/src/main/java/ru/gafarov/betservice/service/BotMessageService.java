package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;

public interface BotMessageService {
    Proto.ResponseMessage save(Proto.BotMessage request);

    Proto.ResponseBotMessage get(Proto.BotMessage request);
}
