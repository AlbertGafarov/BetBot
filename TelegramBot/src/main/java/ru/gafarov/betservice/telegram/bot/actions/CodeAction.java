package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;
import ru.gafarov.betservice.telegram.bot.service.BotService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CodeAction implements Action {

    private final AuthorizationService authorizationService;
    private final BotService botService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = authorizationService.getCode(chatId);
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText(text);
        return List.of(sendMessage);
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String text = authorizationService.getCode(chatId);
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText(text);

        botService.delete(update);
        return List.of(sendMessage);
    }
}
