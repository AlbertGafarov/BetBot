package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Info;
import ru.gafarov.bet.grpcInterface.InfoServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.service.*;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcInfoRequestHandler extends InfoServiceGrpc.InfoServiceImplBase {

    private final InfoService infoService;

    @Override
    public void get(Info.RequestInfo request, StreamObserver<Info.ResponseInfo> responseObserver) {
        responseObserver.onNext(infoService.getInfo(request));
        responseObserver.onCompleted();
    }
}
