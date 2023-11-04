package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;
import ru.gafarov.betservice.telegram.bot.service.BotService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CodeAction implements Action {

    private final AuthorizationService authorizationService;
    private final BotService botService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        authorizationService.getCode(chatId);
        botService.delete(update);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        authorizationService.getCode(chatId);
        botService.delete(update);
        return new ArrayList<>();
    }
}
