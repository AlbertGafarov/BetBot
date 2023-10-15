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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowBetsAction implements Action {

    private final UserService userService;
    private final BetService betService;
    private final PrettyPrinter prettyPrinter;

    @Override
    public List<SendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        Proto.ResponseMessage response = betService.showActiveBets(user);
        List<Proto.Bet> bets = response.getBetsList();
        List<SendMessage> sendMessages = bets.stream().map(bet -> {
            SendMessage msgToUser = new SendMessage();
            msgToUser.setChatId(chatId);
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            msgToUser.setText(prettyPrinter.printBet(bet));
            msgToUser.setParseMode(ParseMode.HTML);
            return msgToUser;
        }).collect(Collectors.toList());
        if (sendMessages.isEmpty()) {
            SendMessage msgToUser = new SendMessage();
            msgToUser.setChatId(chatId);
            msgToUser.setText("У Вас нет активных споров");
            sendMessages.add(msgToUser);
        }
        return sendMessages;
    }

    @Override
    public List<SendMessage> callback(Update update) {
        return null;
    }
}
