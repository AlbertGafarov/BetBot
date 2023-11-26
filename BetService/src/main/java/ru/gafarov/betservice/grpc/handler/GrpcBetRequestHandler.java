package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcBetRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final UserService userService;
    private final BetService betService;

    @Override
    public void addBet(ProtoBet.Bet request, StreamObserver<ProtoBet.ResponseBet> responseObserver) {
        responseObserver.onNext(betService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changeStatusBet(ProtoBet.ChangeStatusBetMessage request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.changeBetStatus(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changeChatStatus(UserOuterClass.User request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(userService.changeChatStatus(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveBets(UserOuterClass.User request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.getActiveBets(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBet(ProtoBet.Bet request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.showBet(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBetsByTemplate(ProtoBet.Bet request, StreamObserver<ProtoBet.ResponseBet> responseObserver) {
        responseObserver.onNext(betService.getBets(request));
        responseObserver.onCompleted();
    }
}
