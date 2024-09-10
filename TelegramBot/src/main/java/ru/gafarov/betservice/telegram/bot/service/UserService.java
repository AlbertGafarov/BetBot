package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.DrBet.ResponseDraftBet;
import ru.gafarov.bet.grpcInterface.DrBetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Friend.Subscribe;
import ru.gafarov.bet.grpcInterface.FriendServiceGrpc;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ResponseUser;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.bet.grpcInterface.UserServiceGrpc;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final FriendServiceGrpc.FriendServiceBlockingStub grpcFriendStub;
    private final UserServiceGrpc.UserServiceBlockingStub grpcUserStub;
    private final DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub;

    public User getUser(long chatId) {
        ResponseUser responseMessage = grpcUserStub.getUser(User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        } else if(Status.NOT_FOUND.equals(responseMessage.getStatus())) {
        return null;
        }
        throw new IllegalStateException("Получена неожиданная ошибка при поиске пользователя");
    }

    public User getUserById(long id) {
        ResponseUser responseMessage = grpcUserStub.getUser(User.newBuilder().setId(id).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }

    public void setChatStatus(User protoUser, ChatStatus chatStatus) {
        setChatStatus(protoUser, chatStatus, null);
    }

    public void setChatStatus(User protoUser, ChatStatus chatStatus, Long betId) {
        val builder = protoUser.getDialogStatus().toBuilder()
                .setChatStatus(chatStatus);
        if (betId != null) {
            builder.setBetId(betId);
        }
        protoUser = User.newBuilder(protoUser)
                .setDialogStatus(builder.build())
                .build();
        log.debug("Меняем статус чата: \n{}", protoUser);
        ResponseUser response = grpcUserStub.changeChatStatus(protoUser);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке установить статус пользователя");
        }
    }

    public User getUser(String username, int code) {
        ResponseUser response = grpcUserStub.getUser(User.newBuilder()
                .setUsername(username)
                .setCode(code).build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public DraftBet getLastDraftBet(User user) {
        ResponseDraftBet response = grpcDrBetStub.getLastDraftBet(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }

    public User findFriendByChatId(User subscriber, long chatId) {
        ResponseUser response = grpcFriendStub.findFriend(Subscribe.newBuilder()
                .setSubscriber(subscriber)
                .setSubscribed(User.newBuilder().setChatId(chatId).build())
                .build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public User findFriendById(User subscriber, long friendId) {
        ResponseUser response = grpcFriendStub.findFriend(Subscribe.newBuilder()
                .setSubscriber(subscriber)
                .setSubscribed(User.newBuilder().setId(friendId).build())
                .build());
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getUser();
        }
        return null;
    }

    public List<User> getSubscribes(User user) {
        ResponseUser response = grpcFriendStub.getSubscribes(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getUsersList();
        } else if (response.getStatus().equals(Status.NOT_FOUND)) {
            return new ArrayList<>();
        }
        log.error("Получена ошибка при попытке получения списка друзей");
        return null;
    }
}