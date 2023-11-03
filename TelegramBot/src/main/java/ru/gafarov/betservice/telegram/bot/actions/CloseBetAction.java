package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CloseBetAction implements Action {

    private final BotService botService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        return null;
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        botService.delete(update);
        return new ArrayList<>();
    }
}