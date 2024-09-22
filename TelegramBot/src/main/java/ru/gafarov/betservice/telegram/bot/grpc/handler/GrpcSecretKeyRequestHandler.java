package ru.gafarov.betservice.telegram.bot.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.SecretKeyServiceGrpc;
import ru.gafarov.betservice.telegram.bot.service.SecretKeyService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcSecretKeyRequestHandler extends SecretKeyServiceGrpc.SecretKeyServiceImplBase {

    private final SecretKeyService secretKeyService;

    // Получить секретный код

    @Override
    public void getSecretMessage(SecretKey.MessageWithKey request, StreamObserver<SecretKey.ResponseSecretKey> responseObserver) {
        responseObserver.onNext(secretKeyService.getSecretMessage(request));
        responseObserver.onCompleted();
    }
    // Сохранить автосгенерированный код в переписке с юзером
    @Override
    public void sendAutoGenerateKeyToUser(SecretKey.MessageWithKey request, StreamObserver<SecretKey.ResponseSecretKey> responseObserver) {
        responseObserver.onNext(secretKeyService.sendAutoGenerateKeyToUser(request));
        responseObserver.onCompleted();
    }
}