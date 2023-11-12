package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.ChatStatus;
import ru.gafarov.bet.grpcInterface.Proto.DraftBet;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;
import ru.gafarov.betservice.telegram.bot.service.draftBet.DraftBetService;

import static ru.gafarov.betservice.telegram.bot.components.Buttons.wantChoseFromFriends;

@Component
@RequiredArgsConstructor
public class CreateAction implements Action {

    private final DraftBetService draftBetService;
    private final UserService userService;
    private final BotService botService;
    private final BotMessageService botMessageService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        DraftBet draftBet = DraftBet.newBuilder()
                .setInitiator(user).build();
        draftBet = draftBetService.saveDraftBet(draftBet);
        userService.setChatStatus(user, ChatStatus.WAIT_OPPONENT_NAME);
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("Введите username оппонента");
        if (!userService.getFriends(user).isEmpty()) {
            sendMessage.setReplyMarkup(wantChoseFromFriends(draftBet));
        }
        botMessageService.deleteWithoutDraft(draftBet, user);
        botService.sendAndSave(sendMessage, user, BotMessageType.ENTER_USERNAME, draftBet);
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {
    }
}
