package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.bet.grpcInterface.UserServiceGrpc;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcUserRequestHandler extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;
    private final MessageWithKeyService messageWithKeyService;

    @Override
    public void addUser(UserOuterClass.User request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.saveUser(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(UserOuterClass.User request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.getProtoUser(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changeChatStatus(UserOuterClass.User request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.changeChatStatus(request));
        responseObserver.onCompleted();
    }

    // Сохранить номер сообщения с секретным кодом
    @Override
    public void saveMessageWithKey(UserOuterClass.MessageWithKey request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(messageWithKeyService.saveMessageWithKey(request));
        responseObserver.onCompleted();
    }
}