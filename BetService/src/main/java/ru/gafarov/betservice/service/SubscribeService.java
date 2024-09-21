package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.Subscribe;
import ru.gafarov.betservice.entity.User;

public interface SubscribeService {
    void checkAndPutForInitiator(Bet bet);

    /**
     * Добавить новую подписку, если ее нет
     * @param bet спор, после принятия которого будет создана подписка
     **/
    void checkAndPutForOpponent(Bet bet);

    Rs.Response addSubscribe(Friend.Subscribe request);

    Rs.Response delete(Friend.Subscribe request);
    Subscribe getSubscribe(User subscriber, User subscribed);

    void update(Subscribe subscribe);
}
