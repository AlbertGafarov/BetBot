package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto.BotMessage;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.Status;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.SubscribeService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AddMeAction implements Action {

    private final BotService botService;
    private final UserService userService;
    private final BotMessageService botMessageService;
    private final SubscribeService subscribeService;

    @Override
    // /addMe/{username}/{code}/{info text}
    public List<BetSendMessage> handle(Update update) {

        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        String[] commands = update.getMessage().getText().split("/");
        User opponent = userService.getUser(commands[2], Integer.parseInt(commands[3]));

        Status userStatus = subscribeService.addSubscribe(user, opponent);
        BetSendMessage sendUserMessage = new BetSendMessage(chatId);
        sendUserMessage.setDelTime(10_000);

        if (userStatus.equals(Status.SUCCESS)) {
            sendUserMessage.setText(opponent.getUsername() + " добавлен(а) в Ваш список");
            int id = botService.sendAndDelete(sendUserMessage);
            botMessageService.save(BotMessage.newBuilder().setTgMessageId(id)
                    .setType(BotMessageType.ADD_OPPONENT).setUser(user).build());
        } else if (userStatus.equals(Status.REDUNDANT)) {
            sendUserMessage.setText(opponent.getUsername() + " был(а) добавлен(а) в Ваш список ранее");
            int id = botService.sendAndDelete(sendUserMessage);
            botMessageService.save(BotMessage.newBuilder().setTgMessageId(id)
                    .setType(BotMessageType.ADD_OPPONENT).setUser(user).build());
        }

        Status opponentStatus = subscribeService.addSubscribe(opponent, user);
        BetSendMessage sendOpponentMessage = new BetSendMessage(opponent.getChatId());
        sendOpponentMessage.setDelTime(10_000);

        if (opponentStatus.equals(Status.SUCCESS)) {
            sendOpponentMessage.setText(user.getUsername() + " добавлен(а) в Ваш список");
            int id = botService.sendAndDelete(sendOpponentMessage);
            botMessageService.save(BotMessage.newBuilder().setTgMessageId(id)
                    .setType(BotMessageType.ADD_OPPONENT).setUser(opponent).build());
        }
        botService.delete(update);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}