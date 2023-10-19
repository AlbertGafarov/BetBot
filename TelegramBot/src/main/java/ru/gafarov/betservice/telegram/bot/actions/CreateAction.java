package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.DeleteService;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateAction implements Action {

    private final DraftBetService draftBetService;
    private final UserService userService;
    private final DeleteService deleteService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        Proto.DraftBet draftBet = Proto.DraftBet.newBuilder().build();
        draftBet = draftBetService.saveDraftBet(draftBet);
        user = user.toBuilder().setDraftBet(draftBet).build();
        userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
        BetSendMessage sendMessage = new BetSendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Введите username оппонента");

        deleteService.delete(update);
        return List.of(sendMessage);
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
