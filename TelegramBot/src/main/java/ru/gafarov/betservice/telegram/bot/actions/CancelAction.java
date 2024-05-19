package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Component
@RequiredArgsConstructor
public class CancelAction implements Action {

    private final BotService botService;
    private final UserService userService;

    @Override
    public void handle(Update update) {
    }

    @Override
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        UserOuterClass.User user = userService.getUser(chatId);
        userService.setChatStatus(user, UserOuterClass.ChatStatus.START, 0L);
        botService.delete(update);
    }
}