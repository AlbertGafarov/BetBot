package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.service.BotService;

@Component
@RequiredArgsConstructor
public class CloseAction implements Action {

    private final BotService botService;

    @Override
    public void handle(Update update) {
    }

    @Override
    public void callback(Update update) {
        botService.delete(update);
    }
}