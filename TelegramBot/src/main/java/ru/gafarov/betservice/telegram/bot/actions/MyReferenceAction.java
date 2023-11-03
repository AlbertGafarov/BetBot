package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MyReferenceAction implements Action {

    private final BotService botService;
    private final UserService userService;
    private final BotMessageService botMessageService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        BetSendMessage sendInfoMessage = new BetSendMessage(chatId);
        sendInfoMessage.setText("Перешлите следующее сообщение вашему собеседнику, чтобы он переслал его в Бот:");

        botMessageService.save(Proto.BotMessage.newBuilder().setTgMessageId(botService.send(sendInfoMessage))
                .setType(Proto.BotMessageType.MY_REFERENCE_INFO).setUser(user).build());

        String text = "/addMe/"+ user.getUsername() + "/" + user.getCode() + "/\n<i>\"Перешлите это сообщение в Бот," +
                " чтобы добавить " + user.getUsername() + "\"</i>";
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode(ParseMode.HTML);

        botMessageService.save(Proto.BotMessage.newBuilder().setTgMessageId(botService.send(sendMessage))
                .setType(Proto.BotMessageType.MY_REFERENCE).setUser(user).build());
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
