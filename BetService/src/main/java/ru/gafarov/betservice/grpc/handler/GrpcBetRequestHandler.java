package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.service.ArgumentService;
import ru.gafarov.betservice.service.BetService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcBetRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final BetService betService;
    private final ArgumentService argumentService;

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
    public void getActiveBets(UserOuterClass.User request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.getActiveBets(request));
        responseObserver.onCompleted();
    }

    @SneakyThrows
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

    @SneakyThrows
    @Override
    public void addArgument(ProtoBet.Argument argument, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(argumentService.save(argument));
        responseObserver.onCompleted();
    }
}
