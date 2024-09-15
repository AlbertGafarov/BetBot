package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Component
@RequiredArgsConstructor
public class SetSecretKeyAction implements Action {

    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final BotService botService;

    @Override
    // /setSecretKeyAction
    public void handle(Update update) {

        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        if (user == null) {
            user = authorizationService.addNewUser(update, chatId);
        }
        userService.setChatStatus(user, UserOuterClass.ChatStatus.WAIT_SECRET_KEY);

        BetSendMessage sendInfoMessage = new BetSendMessage(chatId);
        sendInfoMessage.setText("Введите секретный ключ");
        sendInfoMessage.setDelTime(60_000);
        botService.sendAndSave(sendInfoMessage, user, BotMessageType.ENTER_SECRET_KEY, true);
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {
    }
}