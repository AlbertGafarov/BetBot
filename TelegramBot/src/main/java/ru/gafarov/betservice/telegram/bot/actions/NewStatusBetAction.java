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
public class NewStatusBetAction implements Action {

    private final BetService betService;
    private final PrettyPrinter prettyPrinter;
    private final UserService userService;
    private final BotService botService;


    @Override
    public void handle(Update update) {
    }

    @Override
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        Proto.User user = userService.getUser(chatId);
        String[] command = update.getCallbackQuery().getData().split("/");
        long betId = Long.parseLong(command[3]);
        Proto.ResponseMessage response = betService.setStatus(user, betId, Proto.BetStatus.valueOf(command[2]));
        Proto.Bet bet = response.getBet();

        // Оповещение оппонента если оно есть
        if (!response.getMessageForOpponent().isEmpty()) {
            BetSendMessage msgToOpponent = new BetSendMessage(bet.getOpponent().getChatId());
            msgToOpponent.setText(response.getMessageForOpponent());
            msgToOpponent.setDelTime(60_000);
            msgToOpponent.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(msgToOpponent, bet.getOpponent(), Proto.BotMessageType.NEW_BET_STATUS);

            BetSendMessage msgBetToOpponent = new BetSendMessage(bet.getOpponent().getChatId());
            msgBetToOpponent.setText(prettyPrinter.printBet(bet));
            msgBetToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            msgBetToOpponent.setDelTime(60_000);
            botService.sendAndSave(msgBetToOpponent, bet.getOpponent(), Proto.BotMessageType.BET);

        }
        // Оповещение инициатора если оно есть
        if (!response.getMessageForInitiator().isEmpty()) {
            BetSendMessage msgToInitiator = new BetSendMessage(bet.getInitiator().getChatId());
            msgToInitiator.setText(response.getMessageForInitiator());
            msgToInitiator.setDelTime(60_000);
            msgToInitiator.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(msgToInitiator, bet.getInitiator(), Proto.BotMessageType.NEW_BET_STATUS);

            BetSendMessage msgBetToInitiator = new BetSendMessage(bet.getInitiator().getChatId());
            msgBetToInitiator.setText(prettyPrinter.printBet(bet));
            msgBetToInitiator.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            msgBetToInitiator.setDelTime(60_000);
            botService.sendAndSave(msgBetToInitiator, bet.getInitiator(), Proto.BotMessageType.BET);
        }
        botService.delete(update);
    }
}
