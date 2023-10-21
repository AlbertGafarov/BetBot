package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class DraftAction implements Action {

    private final UserService userService;
    private final PrettyPrinter prettyPrinter;

    @Lazy
    private final BetTelegramBot bot;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        BetSendMessage sendMessage = new BetSendMessage();
        sendMessage.setChatId(chatId);
        if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
            Proto.DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().build();
            String stringBuilder = "Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) +
                    "\nПодтверждаете?";
            sendMessage.setText(stringBuilder);
            sendMessage.setReplyMarkup(Buttons.approveDraftBetButtons());

        } else {
            sendMessage.setText("У вас нет черновиков спора");
            sendMessage.setDelTime(5000);
        }
        // Удаление сообщения у того кто нажал
        int messageId = update.getMessage().getMessageId();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        bot.delete(deleteMessage);

        return List.of(sendMessage);
    }

    @Override
    public List<BetSendMessage> callback(Update update) {

        return new ArrayList<>();
    }
}
