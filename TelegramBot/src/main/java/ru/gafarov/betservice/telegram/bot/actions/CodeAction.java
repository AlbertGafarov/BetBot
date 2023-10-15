package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CodeAction implements Action {

    private final AuthorizationService authorizationService;


    @Override
    public List<SendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = authorizationService.getCode(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return List.of(sendMessage);
    }

    @Override
    public List<SendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String text = authorizationService.getCode(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return List.of(sendMessage);
    }
}
