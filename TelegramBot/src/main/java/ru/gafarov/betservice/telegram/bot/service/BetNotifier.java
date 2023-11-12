package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetNotifier {

    private final PrettyPrinter prettyPrinter;
    private final BotService botService;

    public Proto.ResponseMessage notifyOfExpiredBets(Proto.Bets bets) {
        List<BetSendMessage> sendMessageList = bets.getBetsList().stream().map(b -> {
                    BetSendMessage sendMessage = new BetSendMessage(b.getInitiator().getChatId());
                    sendMessage.setUser(b.getInitiator());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(Buttons.nextStatusesButtons(b.getInitiatorNextStatusesList(), b.getId()));
                    sendMessage.setBotMessageType(Proto.BotMessageType.BET_TIME_IS_UP);
                    return sendMessage;
                }
        ).collect(Collectors.toList());

        List<BetSendMessage> sendMessageToOpponentList = bets.getBetsList().stream().map(b -> {
                    BetSendMessage sendMessage = new BetSendMessage(b.getOpponent().getChatId());
                    sendMessage.setUser(b.getOpponent());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(Buttons.nextStatusesButtons(b.getOpponentNextStatusesList(), b.getId()));
                    sendMessage.setBotMessageType(Proto.BotMessageType.BET_TIME_IS_UP);
                    return sendMessage;
                }
        ).collect(Collectors.toList());
        sendMessageList.addAll(sendMessageToOpponentList);
        botService.sendTimeIsUpMessage(sendMessageList);
        return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.SUCCESS).build();
    }
}