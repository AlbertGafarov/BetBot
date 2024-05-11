package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.FriendServiceGrpc;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.service.impl.FriendServiceImpl;
import ru.gafarov.betservice.service.SubscribeService;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcFriendRequestHandler extends FriendServiceGrpc.FriendServiceImplBase {

    private final UserService userService;
    private final FriendServiceImpl friendService;
    private final SubscribeService subscribeService;

    @Override
    public void getFriendInfo(Friend.Subscribe request, StreamObserver<Friend.ResponseFriendInfo> responseObserver) {
        responseObserver.onNext(friendService.getFriendInfo(request));
        responseObserver.onCompleted();
    }

    @Override
    public void findFriend(Friend.Subscribe request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.findFriend(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getSubscribes(UserOuterClass.User request, StreamObserver<UserOuterClass.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.getSubscribes(request));
        responseObserver.onCompleted();
    }
    @Override
    public void addSubscribe(Friend.Subscribe request, StreamObserver<Rs.Response> responseObserver) {
        responseObserver.onNext(subscribeService.addSubscribe(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSubscribe(Friend.Subscribe request, StreamObserver<Rs.Response> responseObserver) {
        responseObserver.onNext(subscribeService.delete(request));
        responseObserver.onCompleted();
    }
}
