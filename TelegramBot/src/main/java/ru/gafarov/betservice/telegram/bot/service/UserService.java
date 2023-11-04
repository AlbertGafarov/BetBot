package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;

    public User getUser(long chatId) {
        ResponseUser responseMessage = grpcStub.getUser(User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }
    public User getUserById(long id) {
        ResponseUser responseMessage = grpcStub.getUser(User.newBuilder().setId(id).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }

    public void setChatStatus(User protoUser, ChatStatus chatStatus) {
        protoUser = User.newBuilder(protoUser).setChatStatus(chatStatus).build();
        log.debug("Меняем статус чата: \n{}", protoUser);
        ResponseMessage response = grpcStub.changeChatStatus(protoUser);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке установить статус пользователя");
        }
    }

    public User getUser(String username, int code) {
        ResponseUser response = grpcStub.getUser(User.newBuilder()
                .setUsername(username)
                .setCode(code).build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public DraftBet getLastDraftBet(User user) {
        ResponseDraftBet response = grpcStub.getLastDraftBet(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }

    public User findFriend(User subscriber, long chatId) {
        ResponseUser response = grpcStub.findFriend(Subscribe.newBuilder()
                .setSubscriber(subscriber)
                .setSubscribed(User.newBuilder().setChatId(chatId).build())
                .build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public List<User> getFriends(User user) {
        ResponseUser response = grpcStub.getFriends(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getUsersList();
        } else if (response.getStatus().equals(Status.NOT_FOUND)){
            return new ArrayList<>();
        }
        log.error("Получена ошибка при попытке получения списка друзей");
        return null;
    }
}