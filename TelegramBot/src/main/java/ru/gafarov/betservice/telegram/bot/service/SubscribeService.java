package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Friend.Subscribe;
import ru.gafarov.bet.grpcInterface.FriendServiceGrpc;
import ru.gafarov.bet.grpcInterface.Rs.Response;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final FriendServiceGrpc.FriendServiceBlockingStub grpcFriendStub;
    public Status addSubscribe(User subscriber, User subscribed) {
        Subscribe subscribe = Subscribe.newBuilder().setSubscriber(subscriber).setSubscribed(subscribed).build();
        Response response = grpcFriendStub.addSubscribe(subscribe);
        if (response.getStatus().equals(Status.ERROR)) {
             log.error("Не удалось добавить подписку пользователя с id: {} на пользователя с id: {}", subscriber.getId(), subscribed.getId());
        }
        return response.getStatus();
    }

    public Status delete(User user, User friend) {

        Subscribe subscribe = Subscribe.newBuilder().setSubscriber(user)
                .setSubscribed(friend).build();
        Response response = grpcFriendStub.deleteSubscribe(subscribe);
        return response.getStatus();
    }
}
