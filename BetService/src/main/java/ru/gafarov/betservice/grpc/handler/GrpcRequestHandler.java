package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final UserService userService;
    private final BetService betService;

    @Override
    public void addUser(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(userService.saveUser(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addBet(Proto.Bet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changeStatusBet(Proto.ChangeStatusBetMessage request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.changeBetStatus(request));
        responseObserver.onCompleted();
    }
}
