package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;

    public Proto.User getUser(long chatId) {
        Proto.ResponseMessage responseMessage = grpcStub.getUser(Proto.User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }

    public void setChatStatus(Proto.User protoUser, Proto.ChatStatus chatStatus) {
        protoUser = Proto.User.newBuilder(protoUser).setChatStatus(chatStatus).build();
        log.info("Меняем статус чата: \n{}", protoUser);
        grpcStub.changeChatStatus(protoUser);
    }

    public Proto.User getUser(String username, int code) {
        Proto.ResponseMessage responseMessage = grpcStub.getUser(Proto.User.newBuilder()
                .setUsername(username)
                .setCode(code).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }
}