package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
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
        Proto.User user = userService.getUser(chatId);

        BetSendMessage msgToUser = new BetSendMessage(chatId);
        Proto.ResponseMessage response = betService.showBet(user, betId);
        if (Proto.Status.SUCCESS.equals(response.getStatus()) && response.hasBet()) {
            Proto.Bet bet = response.getBet();

            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
        } else {
            msgToUser.setText("Спор с указанным id не найден");
        }
        botService.sendAndSave(msgToUser, user, Proto.BotMessageType.BET);
        //Удаление вызывающей команды
        botService.delete(update);
    }

    @Override

    // /showBet/{id}
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        String[] command = update.getCallbackQuery().getData().split("/");
        long betId = Long.parseLong(command[2]);
        Proto.User user = userService.getUser(chatId);
        BetSendMessage msgToUser = new BetSendMessage(chatId);

        Proto.ResponseMessage response = betService.showBet(user, betId);
        if (Proto.Status.SUCCESS.equals(response.getStatus()) && response.hasBet()) {
            Proto.Bet bet = response.getBet();
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
        } else {
            msgToUser.setText("Спор с указанным id не найден");
        }
        botService.sendAndSave(msgToUser, user, Proto.BotMessageType.BET);
        //Удаление вызывающей команды
        botService.delete(update);
    }
}
