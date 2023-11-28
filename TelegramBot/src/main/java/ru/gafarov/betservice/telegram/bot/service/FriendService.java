package ru.gafarov.betservice.telegram.bot.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.Friend.Subscribe;
import ru.gafarov.bet.grpcInterface.FriendServiceGrpc;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;


@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendServiceGrpc.FriendServiceBlockingStub grpcFriendStub;

    public Friend.FriendInfo getFriendInfo(User user, long friendId) {
        Subscribe subscribe = Subscribe.newBuilder().setSubscriber(user)
                .setSubscribed(User.newBuilder().setId(friendId).build())
                .build();
        Friend.ResponseFriendInfo response = grpcFriendStub.getFriendInfo(subscribe);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getFriendInfo();
        } else {
            log.error("Получена ошибка при попытке получить информацию о друге с id: {}", friendId);
            return null;
        }
    }
}
