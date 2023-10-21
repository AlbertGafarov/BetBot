package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class BotMessageService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;

    public void save(Proto.BotMessage botMessage) {
        grpcStub.saveBotMessage(botMessage);
    }

    public Integer getId(Proto.BotMessage botMessage) {
        Proto.ResponseBotMessage response = grpcStub.getBotMessage(botMessage);
        log.info("Получен ответ с botMessage:\n" + response.toString());
        if (response.getRequestStatus().equals(Proto.RequestStatus.SUCCESS)) {
            return response.getBotMessage().getTgMessageId();
        }
        return null;
    }
}
