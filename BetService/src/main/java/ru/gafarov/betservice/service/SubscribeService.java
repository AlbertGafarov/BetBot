package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.Bet;

public interface SubscribeService {
    void checkAndPutForInitiator(Bet bet);

    void checkAndPutForOpponent(Bet bet);

    Proto.Response addSubscribe(Proto.Subscribe request);

    Proto.Response delete(Proto.Subscribe request);
}
