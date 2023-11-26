package ru.gafarov.betservice.grpc.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.BotMessageServiceGrpc;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.service.BotMessageService;

@Component
@GrpcService
@RequiredArgsConstructor
public class GrpcBotMessageRequestHandler extends BotMessageServiceGrpc.BotMessageServiceImplBase {

    private final BotMessageService botMessageService;

    @Override
    public void getBotMessagesByTemplate(BotMessageOuterClass.BotMessage request, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getAllByTemplate(request));
        responseObserver.onCompleted();
    }


    @Override
    public void saveBotMessage(BotMessageOuterClass.BotMessage request, StreamObserver<ProtoBet.ResponseMessage> responseObserver) {
        responseObserver.onNext(botMessageService.save(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessage(BotMessageOuterClass.BotMessage request, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.get(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessages(DrBet.DraftBet request, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getAll(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBotMessages(BotMessageOuterClass.BotMessages request, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.deleteAll(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBotMessage(BotMessageOuterClass.BotMessage request, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.delete(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getBotMessagesWithout(DrBet.DraftBet draftBet, StreamObserver<BotMessageOuterClass.ResponseBotMessage> responseObserver) {
        responseObserver.onNext(botMessageService.getWithout(draftBet));
        responseObserver.onCompleted();
    }
}
