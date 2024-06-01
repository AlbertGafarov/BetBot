package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.ProtoBet;

public interface ArgumentService {
    /**
     * Добавить аргумент в споре
     * **/
    ProtoBet.ResponseMessage save(ProtoBet.Argument argument);
}
