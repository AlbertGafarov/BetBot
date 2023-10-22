package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class BotMessageService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final BotService botService;

    public void save(Proto.BotMessage botMessage) {
        Proto.ResponseMessage response = grpcStub.saveBotMessage(botMessage);
        if (!response.getStatus().equals(Proto.Status.SUCCESS)) {
            log.error("Ошибка сохранения botMessage id: {}", botMessage.getTgMessageId());
        }
    }

    public Integer getId(Proto.BotMessage botMessage) {
        Proto.ResponseBotMessage response = grpcStub.getBotMessage(botMessage);
        log.info("Получен ответ с botMessage:\n" + response.toString());
        if (response.getStatus().equals(Proto.Status.SUCCESS)) {
            return response.getBotMessage().getTgMessageId();
        }
        return null;
    }

    public void delete(Proto.DraftBet draftBet, Proto.User user) {
        Proto.ResponseBotMessage response = grpcStub.getBotMessages(draftBet);
        if (response.getStatus().equals(Proto.Status.ERROR)) {
            log.error("Получена ошибка при попытке получить botMessages по draftBet c id: {}", draftBet.getId());
        } else {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(a.getTgMessageId());
                deleteMessage.setChatId(user.getChatId());
                botService.delete(deleteMessage, 0);
            });
        }
    }
}
