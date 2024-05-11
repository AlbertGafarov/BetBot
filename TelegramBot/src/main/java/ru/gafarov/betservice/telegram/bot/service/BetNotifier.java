package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.ProtoBet.Bets;
import ru.gafarov.bet.grpcInterface.ProtoBet.ResponseMessage;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.BetButtons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetNotifier {

    private final PrettyPrinter prettyPrinter;
    private final BotService botService;

    public ResponseMessage notifyOfExpiredBets(Bets bets) {
        List<BetSendMessage> sendMessageList = bets.getBetsList().stream().map(b -> {
                    BetSendMessage sendMessage = new BetSendMessage(b.getInitiator().getChatId());
                    sendMessage.setUser(b.getInitiator());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(BetButtons.getBetButtons(b, true));
                    sendMessage.setBotMessageType(BotMessageType.BET_TIME_IS_UP);
                    return sendMessage;
                }
        ).collect(Collectors.toList());

        List<BetSendMessage> sendMessageToOpponentList = bets.getBetsList().stream().map(b -> {
                    BetSendMessage sendMessage = new BetSendMessage(b.getOpponent().getChatId());
                    sendMessage.setUser(b.getOpponent());
                    sendMessage.setText("<b>Наступила дата окончания:</b>\n" + prettyPrinter.printBet(b));
                    sendMessage.setReplyMarkup(BetButtons.getBetButtons(b, false));
                    sendMessage.setBotMessageType(BotMessageType.BET_TIME_IS_UP);
                    return sendMessage;
                }
        ).collect(Collectors.toList());
        sendMessageList.addAll(sendMessageToOpponentList);
        botService.sendTimeIsUpMessage(sendMessageList);
        return ResponseMessage.newBuilder().setStatus(Status.SUCCESS).build();
    }
}