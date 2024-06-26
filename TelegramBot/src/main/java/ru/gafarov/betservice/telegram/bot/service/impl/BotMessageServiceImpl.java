package ru.gafarov.betservice.telegram.bot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessages;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.ResponseBotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageServiceGrpc;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.DeleteMessageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessageServiceImpl implements BotMessageService {

    private final BotMessageServiceGrpc.BotMessageServiceBlockingStub grpcStub;
    private final DeleteMessageService deleteMessageService;

    @Override
    public void save(BotMessage botMessage) {
        ResponseMessage response = grpcStub.saveBotMessage(botMessage);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Ошибка сохранения botMessage id: {}", botMessage.getTgMessageId());
        }
    }

    @Override
    public Integer getId(BotMessage botMessage) {
        ResponseBotMessage response = grpcStub.getBotMessage(botMessage);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getBotMessage().getTgMessageId();
        }
        return null;
    }

    @Override
    public void deleteByDraft(DraftBet draftBet, User user) {
        ResponseBotMessage response = grpcStub.getBotMessages(draftBet);
        if (response.getStatus().equals(Status.ERROR)) {
            log.error("Получена ошибка при попытке получить botMessages по draftBet c id: {}", draftBet.getId());
        } else {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(user.getChatId()), a.getTgMessageId());
                deleteMessageService.deleteSync(deleteMessage);
            });
            ResponseBotMessage responseBotMessage = grpcStub.deleteBotMessages(BotMessages.newBuilder()
                    .addAllBotMessage(response.getBotMessagesList()).build());
            if (!responseBotMessage.getStatus().equals(Status.SUCCESS)) {
                log.error("Получена ошибка при попытке пометить в БД сообщения от бота удаленными");
            }
        }
    }

    @Override
    public void markDeleted(DeleteMessage deleteMessage) {

        ResponseBotMessage responseBotMessage = grpcStub.deleteBotMessage(BotMessage.newBuilder()
                .setTgMessageId(deleteMessage.getMessageId()).build());
        if (!responseBotMessage.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке пометить в БД сообщения от бота удаленными");
        }
    }

    @Override
    public boolean isNotDeleted(Integer messageId) {

        ResponseBotMessage response = grpcStub.getBotMessage(BotMessage.newBuilder().setTgMessageId(messageId).build());
        if (response.hasBotMessage()) {
            return !response.getBotMessage().getIsDeleted();
        } else {
            log.error("botMessage с tg_message_id: {} не найден", messageId);
            return true;
        }
    }

    @Override
    public void deleteWithoutDraft(DraftBet draftBet, User user) {
        ResponseBotMessage response = grpcStub.getBotMessagesWithout(draftBet);
        if (response.getStatus().equals(Status.ERROR)) {
            log.error("Получена ошибка при попытке получить botMessages кроме draftBet c id: {}", draftBet.getId());
        } else {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(user.getChatId()), a.getTgMessageId());
                deleteMessageService.deleteSync(deleteMessage);
            });
            ResponseBotMessage responseBotMessage = grpcStub.deleteBotMessages(BotMessages.newBuilder()
                    .addAllBotMessage(response.getBotMessagesList()).build());
            if (responseBotMessage.getStatus().equals(Status.ERROR)) {
                log.error("Получена ошибка при попытке пометить в БД сообщения от бота удаленными");
            }
        }
    }

    @Override
    public void deleteByBotMessageType(User user, BotMessageType botMessageType) {
        ResponseBotMessage response = grpcStub.getBotMessagesByTemplate(BotMessage.newBuilder().setUser(user).setType(botMessageType).build());
        log.info("Получено {} сообщений", response.getBotMessagesCount());
        if (response.getStatus().equals(Status.SUCCESS)) {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(user.getChatId()), a.getTgMessageId());
                deleteMessageService.deleteSync(deleteMessage);
            });
        }
    }

    @Override
    public void deleteBotMessagesByTemplate(User user, BotMessageType botMessageType, User friend, Bet bet) {

        val builder = BotMessage.newBuilder().setUser(user);
        if (botMessageType != null) {
            builder.setType(botMessageType);
        }
        if (friend != null) {
            builder.setFriend(friend);
        }
        if (bet != null) {
            builder.mergeBet(bet).setBet(bet);
        }

        ResponseBotMessage response = grpcStub.getBotMessagesByTemplate(builder.build());
        log.info("Получено {} сообщений", response.getBotMessagesCount());
        if (response.getStatus().equals(Status.SUCCESS)) {
            response.getBotMessagesList().forEach(a -> {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(user.getChatId()), a.getTgMessageId());
                deleteMessageService.deleteSync(deleteMessage);
            });
        }
    }
}
