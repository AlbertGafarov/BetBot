package ru.gafarov.betservice.telegram.bot.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.ProtoBet.Bets;
import ru.gafarov.bet.grpcInterface.ProtoBet.ResponseMessage;
import ru.gafarov.betservice.telegram.bot.service.BetNotifier;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final BetNotifier betNotifier;
    @Override
    public void notifyOfExpiredBets(Bets request, StreamObserver<ResponseMessage> responseObserver) {
        responseObserver.onNext(betNotifier.notifyOfExpiredBets(request));
        responseObserver.onCompleted();
    }
}