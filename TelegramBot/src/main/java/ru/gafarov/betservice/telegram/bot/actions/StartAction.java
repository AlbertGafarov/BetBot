package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartAction implements Action {

    private final AuthorizationService authorizationService;

    @Override
    public List<SendMessage> handle(Update update) {
        return List.of(authorizationService.authorization(update));
    }

    @Override
    public List<SendMessage> callback(Update update) {
        return null;
    }
}
