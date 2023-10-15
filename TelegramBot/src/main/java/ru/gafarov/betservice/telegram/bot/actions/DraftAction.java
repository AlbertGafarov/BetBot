package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DraftAction implements Action {

    private final UserService userService;
    private final PrettyPrinter prettyPrinter;

    @Override
    public List<SendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
            Proto.DraftBet draftBet = user.getDraftBet().toBuilder().build();
            String stringBuilder = "Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) +
                    "\nПодтверждаете?";
            sendMessage.setText(stringBuilder);
            sendMessage.setReplyMarkup(Buttons.approveDraftBetButtons());
        } else {
            sendMessage.setText("У вас нет черновиков спора");
        }
        return List.of(sendMessage);
    }

    @Override
    public List<SendMessage> callback(Update update) {

        return new ArrayList<>();
    }
}
