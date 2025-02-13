package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.bet.grpcInterface.UserServiceGrpc;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcUserRequestHandler extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

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

    @Override
    public void setEncryptionStatus(UserOuterClass.User user, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.changeEncryptedStatus(user));
        responseObserver.onCompleted();
    }
}