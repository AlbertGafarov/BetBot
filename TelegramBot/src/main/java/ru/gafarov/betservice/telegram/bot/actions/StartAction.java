package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class StartAction implements Action {

    private final AuthorizationService authorizationService;

    @Lazy
    private final BetTelegramBot bot;

    @Override
    public List<BetSendMessage> handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        BetSendMessage sendMessage = authorizationService.authorization(update);
        int messageId = update.getMessage().getMessageId();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        bot.delete(deleteMessage);
        return List.of(sendMessage);
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
