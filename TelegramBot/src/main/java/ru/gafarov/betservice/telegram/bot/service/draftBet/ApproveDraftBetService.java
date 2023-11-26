package ru.gafarov.betservice.telegram.bot.service.draftBet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveDraftBetService {

    private final UserService userService;
    private final BetService betService;
    private final DraftBetService draftBetService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;

    // /draftBet/{id}/approve/(ok|cancel)
    public void approveOrCancelDraft(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);
        if (ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
            String[] command = update.getCallbackQuery().getData().split("/");
            DraftBet draftBet = draftBetService.getByIdAndUser(Long.valueOf(command[2]), user);
            log.debug("Подготовленный спор: {}", draftBet);

            botService.delete(update);
            userService.setChatStatus(user, ChatStatus.START);
            draftBetService.delete(draftBet);
            botMessageService.deleteByDraft(draftBet, user);

            switch (command[4]) {
                case "ok":
                    Bet bet = betService.addBet(draftBet, user);

                    // Предложение оппоненту нового спора
                    BetSendMessage offerToOpponent = new BetSendMessage(bet.getOpponent().getChatId());
                    offerToOpponent.setText(prettyPrinter.printOfferBet(bet));
                    offerToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));

                    // Подтверждение инициатору об отправке
                    BetSendMessage msgDeliveryToInitiator = new BetSendMessage(bet.getInitiator().getChatId());
                    msgDeliveryToInitiator.setText("Предложение о споре отправлено оппоненту");
                    msgDeliveryToInitiator.setDelTime(10_000);

                    botService.sendAndSave(offerToOpponent, bet.getOpponent(), BotMessageType.OFFER_BET);
                    botService.sendAndSave(msgDeliveryToInitiator, user, BotMessageType.APPROVE_DRAFT);
                    break;

                case "cancel":
                    BetSendMessage msgToInitiator = new BetSendMessage(chatId);
                    msgToInitiator.setText("Спор отклонен. Черновик удален");
                    msgToInitiator.setDelTime(10_000);

                    botService.sendAndSaveDraftBet(msgToInitiator, user, BotMessageType.CANCEL_DRAFT, draftBet);
                    break;
            }
        }
    }
}
