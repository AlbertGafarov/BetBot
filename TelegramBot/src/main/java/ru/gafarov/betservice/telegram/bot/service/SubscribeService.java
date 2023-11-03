package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.Response;
import ru.gafarov.bet.grpcInterface.Proto.Status;
import ru.gafarov.bet.grpcInterface.Proto.Subscribe;
import ru.gafarov.bet.grpcInterface.Proto.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    public Status addSubscribe(User subscriber, User subscribed) {
        Subscribe subscribe = Subscribe.newBuilder().setSubscriber(subscriber).setSubscribed(subscribed).build();
        Response response = grpcStub.addSubscribe(subscribe);
        if (response.getStatus().equals(Status.ERROR)) {
             log.error("Не удалось добавить подписку пользователя с id: {} на пользователя с id: {}", subscriber.getId(), subscribed.getId());
        }
        return response.getStatus();
    }
}
