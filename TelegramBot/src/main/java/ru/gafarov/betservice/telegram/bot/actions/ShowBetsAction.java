package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto.Bet;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.ResponseMessage;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot.READ_ONE_CHAR_MS;
import static ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot.WAIT_NEXT_MESSAGE_MS;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowBetsAction implements Action {

    private final UserService userService;
    private final BetService betService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        ResponseMessage response = betService.showActiveBets(user);
        List<Bet> bets = response.getBetsList();
        AtomicInteger i = new AtomicInteger();
        i.set(0);
        List<BetSendMessage> sendMessages = bets.stream().sorted((o1, o2) -> {
            LocalDateTime time1 = prettyPrinter.fromGoogleTimestampUTC(o1.getFinishDate());
            LocalDateTime time2 = prettyPrinter.fromGoogleTimestampUTC(o2.getFinishDate());
            return time1.compareTo(time2);
        }).map(bet -> {
            BetSendMessage msgToUser = new BetSendMessage(chatId);
            if (bet.getInitiator().getUsername().equals(user.getUsername())
                    && bet.getInitiator().getCode() == user.getCode()) {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
            } else {
                msgToUser.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
            }
            String text = prettyPrinter.printBet(bet);
            msgToUser.setText(text);
            msgToUser.setDelTime(i.accumulateAndGet(READ_ONE_CHAR_MS * text.length() + WAIT_NEXT_MESSAGE_MS, Integer::sum));
            return msgToUser;
        }).collect(Collectors.toList());
        if (sendMessages.isEmpty()) {
            BetSendMessage msgToUser = new BetSendMessage(chatId);
            msgToUser.setText("У Вас нет активных споров");
            botService.sendAndSave(msgToUser, user, BotMessageType.YOU_HAVE_NOT_BETS);
        } else {
            for (BetSendMessage sendMessage : sendMessages) {
                botService.sendAndSave(sendMessage, user, BotMessageType.BET);
                try {
                    Thread.sleep(WAIT_NEXT_MESSAGE_MS);
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }

        //Удаление вызывающей команды
        botService.delete(update);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
