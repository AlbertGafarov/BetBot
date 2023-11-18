package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.service.*;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcRequestHandler extends BetServiceGrpc.BetServiceImplBase {

    private final UserService userService;
    private final BetService betService;
    private final DraftBetService draftBetService;
    private final BotMessageService botMessageService;
    private final SubscribeService subscribeService;

    @Override
    public void addUser(Proto.User request, StreamObserver<Proto.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.saveUser(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addBet(Proto.Bet request, StreamObserver<Proto.ResponseBet> responseObserver) {
        responseObserver.onNext(betService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changeStatusBet(Proto.ChangeStatusBetMessage request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.changeBetStatus(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(Proto.User request, StreamObserver<Proto.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.getProtoUser(request));
        responseObserver.onCompleted();
    }
    @Override
    public void changeChatStatus(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(userService.changeChatStatus(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessagesByType(Proto.BotMessage request, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getAllByType(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addDraftBet(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentName(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentName(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setOpponentCode(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setOpponentCodeAndName(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setDefinition(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setDefinition(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setWager(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setWager(request));
        responseObserver.onCompleted();
    }

    @Override
    public void setFinishDate(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.setFinishDate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveBets(Proto.User request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.getActiveBets(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBet(Proto.Bet request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(betService.showBet(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getLastDraftBet(Proto.User request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.getLastDraftBet(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDraftBet(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.delete(request));
        responseObserver.onCompleted();
    }

    @Override
    public void saveBotMessage(Proto.BotMessage request, StreamObserver<Proto.ResponseMessage> responseObserver) {
        responseObserver.onNext(botMessageService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessage(Proto.BotMessage request, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.get(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessages(Proto.DraftBet request, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getAll(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getDraftBet(Proto.DraftBet request, StreamObserver<Proto.ResponseDraftBet> responseObserver) {
        responseObserver.onNext(draftBetService.getDraftBet(request));
        responseObserver.onCompleted();
    }

    @Override
    public void findFriend(Proto.Subscribe request, StreamObserver<Proto.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.findFriend(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getFriends(Proto.User request, StreamObserver<Proto.ResponseUser> responseObserver) {
        responseObserver.onNext(userService.getFriends(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBotMessages(Proto.BotMessages request, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.deleteAll(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBotMessage(Proto.BotMessage request, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.delete(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addSubscribe(Proto.Subscribe request, StreamObserver<Proto.Response> responseObserver) {
        responseObserver.onNext(subscribeService.addSubscribe(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSubscribe(Proto.Subscribe request, StreamObserver<Proto.Response> responseObserver) {
        responseObserver.onNext(subscribeService.delete(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessagesWithout(Proto.DraftBet draftBet, StreamObserver<Proto.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getWithout(draftBet));
        responseObserver.onCompleted();
    }
}
