package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;
import ru.gafarov.betservice.telegram.bot.service.BotService;

@Component
@RequiredArgsConstructor
public class StartAction implements Action {

    private final AuthorizationService authorizationService;
    private final BotService botService;

    @Override
    public void handle(Update update) {
        authorizationService.authorization(update);
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {
    }
}