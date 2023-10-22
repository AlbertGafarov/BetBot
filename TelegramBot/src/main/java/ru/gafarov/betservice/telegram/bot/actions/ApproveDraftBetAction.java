package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveDraftBetAction implements Action {

    private final UserService userService;
    private final BetService betService;
    private final DraftBetService draftBetService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;

    @Override
    public List<BetSendMessage> handle(Update update) {
        return null;
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        Proto.User user = userService.getUser(chatId);
        if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
            String[] command = update.getCallbackQuery().getData().split("/");
            Proto.DraftBet draftBet = draftBetService.getByIdAndUser(Long.valueOf(command[3]), user);
            log.info("Подготовленный спор: {}", draftBet);

            botService.delete(update);
            userService.setChatStatus(user, Proto.ChatStatus.START);
            draftBetService.delete(draftBet);
            botMessageService.delete(draftBet, user);

            switch (command[2]) {
                case "ok":
                    Proto.Bet bet = betService.addBet(draftBet, user);

                    // Предложение оппоненту нового спора
                    BetSendMessage offerToOpponent = new BetSendMessage();
                    offerToOpponent.setChatId(bet.getOpponent().getChatId());
                    offerToOpponent.setText(prettyPrinter.printOfferBet(bet));
                    offerToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
                    offerToOpponent.setParseMode(ParseMode.HTML);

                    // Подтверждение инициатору об отправке
                    BetSendMessage msgDeliveryToInitiator = new BetSendMessage();
                    msgDeliveryToInitiator.setChatId(bet.getInitiator().getChatId());
                    msgDeliveryToInitiator.setText("Предложение о споре отправлено оппоненту");
                    msgDeliveryToInitiator.setParseMode(ParseMode.HTML);
                    msgDeliveryToInitiator.setDelTime(10_000);

                    botService.send(offerToOpponent);
                    botService.send(msgDeliveryToInitiator);
                    break;

                case "cancel":
                    BetSendMessage msgToInitiator = new BetSendMessage();
                    msgToInitiator.setChatId(chatId);
                    msgToInitiator.setText("Спор отклонен. Черновик удален");
                    msgToInitiator.setParseMode(ParseMode.HTML);
                    msgToInitiator.setDelTime(10_000);

                    botService.send(msgToInitiator);
                    break;
            }
        }
        return new ArrayList<>();
    }
}
