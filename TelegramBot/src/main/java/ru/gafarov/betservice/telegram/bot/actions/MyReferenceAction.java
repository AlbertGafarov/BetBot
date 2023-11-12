package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Component
@RequiredArgsConstructor
public class MyReferenceAction implements Action {

    private final BotService botService;
    private final UserService userService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        BetSendMessage sendInfoMessage = new BetSendMessage(chatId);
        sendInfoMessage.setText("Перешлите следующее сообщение вашему собеседнику, чтобы он переслал его в Бот:");
        sendInfoMessage.setDelTime(60_000);
        botService.sendAndSave(sendInfoMessage, user, BotMessageType.MY_REFERENCE_INFO);

        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("/addMe/"+ user.getUsername() + "/" + user.getCode() + "/\n<i>\"Перешлите это сообщение в Бот," +
                " чтобы добавить " + user.getUsername() + "\"</i>");
        sendMessage.setDelTime(60_000);
        botService.sendAndSave(sendMessage, user, BotMessageType.MY_REFERENCE);
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {
    }
}
