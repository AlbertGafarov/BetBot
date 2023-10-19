package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CloseBetAction implements Action {

    @Lazy
    private final BetTelegramBot bot;

    @Override
    public List<BetSendMessage> handle(Update update) {
        return null;
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        bot.delete(new DeleteMessage(String.valueOf(chatId), messageId));
        return new ArrayList<>();
    }
}