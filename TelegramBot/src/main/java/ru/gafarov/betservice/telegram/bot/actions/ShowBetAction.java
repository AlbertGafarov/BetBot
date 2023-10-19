package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ShowBetAction implements Action {

    private final UserService userService;
    private final BetService betService;
    private final PrettyPrinter prettyPrinter;

    @Lazy
    private final BetTelegramBot bot;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String[] command = update.getMessage().getText().split("/");
        long betId = Long.parseLong(command[2]);
        Proto.User user = userService.getUser(chatId);

        List<BetSendMessage> sendMessages = new ArrayList<>();
        Proto.ResponseMessage response = betService.showBet(user, betId);
        if (Proto.RequestStatus.SUCCESS.equals(response.getRequestStatus()) && response.hasBet()) {
            Proto.Bet bet = response.getBet();

            BetSendMessage msgToUser = new BetSendMessage();
            msgToUser.setChatId(chatId);
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
            msgToUser.setParseMode(ParseMode.HTML);
            sendMessages.add(msgToUser);
        }
        if (sendMessages.isEmpty()) {
            BetSendMessage msgToUser = new BetSendMessage();
            msgToUser.setChatId(chatId);
            msgToUser.setText("Спор с указанным id не найден");
            sendMessages.add(msgToUser);
        }
        //Удаление вызывающей команды
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), update.getMessage().getMessageId());
        bot.delete(deleteMessage);
        return sendMessages;
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String[] command = update.getCallbackQuery().getData().split("/");
        long betId = Long.parseLong(command[2]);
        Proto.User user = userService.getUser(chatId);

        List<BetSendMessage> sendMessages = new ArrayList<>();
        Proto.ResponseMessage response = betService.showBet(user, betId);
        if (Proto.RequestStatus.SUCCESS.equals(response.getRequestStatus()) && response.hasBet()) {
            Proto.Bet bet = response.getBet();
            BetSendMessage msgToUser = new BetSendMessage();
            msgToUser.setChatId(chatId);
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
            msgToUser.setParseMode(ParseMode.HTML);
            sendMessages.add(msgToUser);
        }
        if (sendMessages.isEmpty()) {
            BetSendMessage msgToUser = new BetSendMessage();
            msgToUser.setChatId(chatId);
            msgToUser.setText("Спор с указанным id не найден");
            sendMessages.add(msgToUser);
        }
        //Удаление вызывающей команды
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), update.getCallbackQuery().getMessage().getMessageId());
        bot.delete(deleteMessage);
        return sendMessages;
    }
}
