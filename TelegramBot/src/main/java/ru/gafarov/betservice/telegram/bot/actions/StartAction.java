package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StartAction implements Action {

    private final AuthorizationService authorizationService;


    @Override
    public List<BetSendMessage> handle(Update update) {
        authorizationService.authorization(update);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
