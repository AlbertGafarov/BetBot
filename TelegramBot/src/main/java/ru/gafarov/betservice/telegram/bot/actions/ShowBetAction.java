package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.BetButtons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowBetAction implements Action {

    private final UserService userService;
    private final BetService betService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String[] command = update.getMessage().getText().split("/");
        long betId = Long.parseLong(command[2]);
        User user = userService.getUser(chatId);

        BetSendMessage msgToUser = new BetSendMessage(chatId);
        ResponseMessage response = betService.showBet(user, betId);
        Bet bet = null;
        if (Status.SUCCESS.equals(response.getStatus()) && response.hasBet()) {
            bet = response.getBet();

            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, true));
            } else {
                msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, false));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
        } else {
            msgToUser.setText("Спор с указанным id не найден");
        }
        botService.sendAndSaveBet(msgToUser, user, BotMessageType.BET, bet);
        //Удаление вызывающей команды
        botService.delete(update);
    }

    @Override

    // /showBet/{id}
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String[] command = update.getCallbackQuery().getData().split("/");
        long betId = Long.parseLong(command[2]);
        User user = userService.getUser(chatId);
        BetSendMessage msgToUser = new BetSendMessage(chatId);

        ResponseMessage response = betService.showBet(user, betId);
        Bet bet = null;
        if (Status.SUCCESS.equals(response.getStatus()) && response.hasBet()) {
            bet = response.getBet();
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, true));
            } else {
                msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, false));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
        } else {
            msgToUser.setText("Спор с указанным id не найден");
        }
        botService.sendAndSaveBet(msgToUser, user, BotMessageType.BET, bet);
        //Удаление вызывающей команды
        botService.delete(update);
    }
}
