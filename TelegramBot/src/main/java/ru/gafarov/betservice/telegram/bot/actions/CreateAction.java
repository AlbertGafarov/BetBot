package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.draftBet.DraftBetService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static ru.gafarov.betservice.telegram.bot.components.Buttons.wantChoseFromFriends;

@Component
@RequiredArgsConstructor
public class CreateAction implements Action {

    private final DraftBetService draftBetService;
    private final UserService userService;
    private final BotService botService;
    private final BotMessageService botMessageService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        Proto.DraftBet draftBet = Proto.DraftBet.newBuilder()
                .setInitiator(user).build();
        draftBet = draftBetService.saveDraftBet(draftBet);
        userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("Введите username оппонента");
        sendMessage.setReplyMarkup(wantChoseFromFriends(draftBet));

        botService.delete(update);
        int id = botService.send(sendMessage);

        Proto.BotMessage botMessage = Proto.BotMessage.newBuilder()
                .setType(Proto.BotMessageType.ENTER_USERNAME)
                .setTgMessageId(id)
                .setDraftBet(draftBet)
                .setUser(user)
                .build();
        botMessageService.save(botMessage);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
