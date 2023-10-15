package ru.gafarov.betservice.telegram.bot.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.service.BetNotifier;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final BetNotifier betNotifier;
    @Override
    public void notifyOfExpiredBets(Proto.Bets request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betNotifier.notifyOfExpiredBets(request));
        responseObserver.onCompleted();
    }
}