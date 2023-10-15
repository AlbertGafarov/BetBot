package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetNotifier {

    private final PrettyPrinter prettyPrinter;
    private final BetTelegramBot bot;

    public Proto.ResponseMessage notifyOfExpiredBets(Proto.Bets bets) {
        List<SendMessage> sendMessageList = bets.getBetsList().stream().map(b -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(b.getInitiator().getChatId());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(Buttons.nextStatusesButtons(b.getInitiatorNextStatusesList(), b.getId()));
                    sendMessage.setParseMode(ParseMode.HTML);
                    return sendMessage;
                }
        ).collect(Collectors.toList());

        List<SendMessage> sendMessageToOpponentList = bets.getBetsList().stream().map(b -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(b.getOpponent().getChatId());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(Buttons.nextStatusesButtons(b.getOpponentNextStatusesList(), b.getId()));
                    sendMessage.setParseMode(ParseMode.HTML);
                    return sendMessage;
                }
        ).collect(Collectors.toList());
        sendMessageList.addAll(sendMessageToOpponentList);
        bot.send(sendMessageList);
        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.SUCCESS).build();
    }
}
