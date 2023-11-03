package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessageService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final BotService botService;

    public void save(BotMessage botMessage) {
        ResponseMessage response = grpcStub.saveBotMessage(botMessage);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Ошибка сохранения botMessage id: {}", botMessage.getTgMessageId());
        }
    }

    public Integer getId(BotMessage botMessage) {
        ResponseBotMessage response = grpcStub.getBotMessage(botMessage);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getBotMessage().getTgMessageId();
        }
        return null;
    }

    public void delete(DraftBet draftBet, User user) {
        ResponseBotMessage response = grpcStub.getBotMessages(draftBet);
        if (response.getStatus().equals(Status.ERROR)) {
            log.error("Получена ошибка при попытке получить botMessages по draftBet c id: {}", draftBet.getId());
        } else {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(a.getTgMessageId());
                deleteMessage.setChatId(user.getChatId());
                botService.deleteAsync(deleteMessage, 0);
            });
            ResponseBotMessage responseBotMessage = grpcStub.deleteBotMessages(BotMessages.newBuilder()
                    .addAllBotMessage(response.getBotMessagesList()).build());
            if (!responseBotMessage.getStatus().equals(Status.SUCCESS)) {
                log.error("Получена ошибка при попытке пометить в БД сообщения от бота удаленными");
            }
        }
    }

    public void markDeleted(DeleteMessage deleteMessage) {

        ResponseBotMessage responseBotMessage = grpcStub.deleteBotMessages(BotMessages.newBuilder()
                .addBotMessage(BotMessage.newBuilder().setTgMessageId(deleteMessage.getMessageId()).build()).build());
        if (!responseBotMessage.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке пометить в БД сообщения от бота удаленными");
        }
    }

    public boolean isDeleted(Integer messageId) {

        ResponseBotMessage response = grpcStub.getBotMessage(BotMessage.newBuilder().setTgMessageId(messageId).build());
        if (response.hasBotMessage()) {
            return response.getBotMessage().getIsDeleted();
        } else {
            log.error("botMessage с tg_message_id: {} не найден", messageId);
            return false;
        }
    }
}
