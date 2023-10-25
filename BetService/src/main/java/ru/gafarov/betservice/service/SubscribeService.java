package ru.gafarov.betservice.service;

import ru.gafarov.betservice.model.Bet;

public interface SubscribeService {
    void checkAndPutForInitiator(Bet bet);

    void checkAndPutForOpponent(Bet bet);
}
