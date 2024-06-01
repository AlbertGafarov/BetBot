package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.Buttons;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Component
@RequiredArgsConstructor
public class ArgumentAction implements Action {

    private final BotService botService;
    private final UserService userService;

    @Override
    public void handle(Update update) {
    }

    @Override
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String[] command = update.getCallbackQuery().getData().split("/");
        UserOuterClass.User user = userService.getUser(chatId);
        if ("write".equals(command[2])) {
            BetSendMessage sendMessage = new BetSendMessage(chatId); // Ответное сообщение
            sendMessage.setText("Введите ваш аргумент в споре");
            sendMessage.setReplyMarkup(Buttons.oneButton("Отмена", "/cancel"));
            botService.sendAndSave(sendMessage, user, BotMessageOuterClass.BotMessageType.ADD_ARGUMENT, true);
            userService.setChatStatus(user, UserOuterClass.ChatStatus.WAIT_ARGUMENT, Long.parseLong(command[3]));
        }
    }
}