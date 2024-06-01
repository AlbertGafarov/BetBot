package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.DrBetServiceGrpc;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.service.DraftBetService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcDraftBetRequestHandler extends DrBetServiceGrpc.DrBetServiceImplBase {

    private final DraftBetService draftBetService;

    @Override
    public void addDraftBet(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentName(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentName(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentCode(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentCodeAndName(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setDefinition(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setDefinition(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setWager(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setWager(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setFinishDate(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setFinishDate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getLastDraftBet(UserOuterClass.User request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.getLastDraftBet(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDraftBet(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.delete(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getDraftBet(DrBet.DraftBet request, StreamObserver<DrBet.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.getDraftBet(request));
        responseObserver.onCompleted();
    }
}
