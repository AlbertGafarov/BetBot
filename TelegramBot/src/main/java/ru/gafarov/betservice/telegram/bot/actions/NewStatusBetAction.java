package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewStatusBetAction implements Action {

    private final BetService betService;
    private final PrettyPrinter prettyPrinter;
    private final UserService userService;


    @Override
    public List<SendMessage> handle(Update update) {
        return null;
    }

    @Override
    public List<SendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        Proto.User user = userService.getUser(chatId);
        String[] command = update.getCallbackQuery().getData().split("/");
        long betId = Long.parseLong(command[3]);
        Proto.ResponseMessage response = betService.setStatus(user, betId, Proto.BetStatus.valueOf(command[2]));
        Proto.Bet bet = response.getBet();

        List<SendMessage> sendMessageList = new ArrayList<>();
        // Оповещение оппонента если оно есть
        if (!response.getMessageForOpponent().isEmpty()) {
            SendMessage msgToOpponent = new SendMessage();
            msgToOpponent.setChatId(bet.getOpponent().getChatId());
            msgToOpponent.setText(response.getMessageForOpponent());
            msgToOpponent.setParseMode(ParseMode.HTML);

            SendMessage msgBetToOpponent = new SendMessage();
            msgBetToOpponent.setChatId(bet.getOpponent().getChatId());
            msgBetToOpponent.setText(prettyPrinter.printBet(bet));
            msgBetToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            msgBetToOpponent.setParseMode(ParseMode.HTML);
            sendMessageList.addAll(List.of(msgBetToOpponent, msgToOpponent));
        }
        // Оповещение инициатора если оно есть
        if (!response.getMessageForInitiator().isEmpty()) {
            SendMessage msgToInitiator = new SendMessage();
            msgToInitiator.setChatId(bet.getInitiator().getChatId());
            msgToInitiator.setText(response.getMessageForInitiator());
            msgToInitiator.setParseMode(ParseMode.HTML);

            SendMessage msgBetToInitiator = new SendMessage();
            msgBetToInitiator.setChatId(bet.getInitiator().getChatId());
            msgBetToInitiator.setText(prettyPrinter.printBet(bet));
            msgBetToInitiator.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            msgBetToInitiator.setParseMode(ParseMode.HTML);

            sendMessageList.addAll(List.of(msgToInitiator, msgBetToInitiator));
        }
        return sendMessageList;
    }
}
