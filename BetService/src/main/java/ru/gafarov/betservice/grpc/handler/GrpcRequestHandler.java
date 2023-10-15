package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.DraftBetService;
import ru.gafarov.betservice.service.UserService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final UserService userService;
    private final BetService betService;
    private final DraftBetService draftBetService;

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

    @Override
    public void getUser(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(userService.getProtoUser(request));
        responseObserver.onCompleted();
    }
    @Override
    public void changeChatStatus(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(userService.changeChatStatus(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addDraftBet(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentName(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentName(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentCode(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentCode(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setDefinition(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.setDefinition(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setWager(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.setWager(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setFinishDate(Proto.DraftBet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(draftBetService.setFinishDate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveBets(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.getActiveBets(request));
        responseObserver.onCompleted();
    }
}
