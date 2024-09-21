package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.SecretKeyServiceGrpc;
import ru.gafarov.betservice.service.MessageWithKeyService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcSecretKeyRequestHandler extends SecretKeyServiceGrpc.SecretKeyServiceImplBase {

    private final MessageWithKeyService messageWithKeyService;

    // Сохранить номер сообщения с секретным кодом
    @Override
    public void saveMessageWithKey(SecretKey.MessageWithKey request, StreamObserver<Rs.Response> responseObserver) {
        responseObserver.onNext(messageWithKeyService.saveMessageWithKey(request));
        responseObserver.onCompleted();
    }
}