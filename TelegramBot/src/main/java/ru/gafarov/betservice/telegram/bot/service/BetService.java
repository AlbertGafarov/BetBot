package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.BetButtons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

import java.util.List;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class BetService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final UserService userService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;

    public Bet addBet(DraftBet draftBet, User user) {
        User opponent = userService.getUser(draftBet.getOpponentName(), draftBet.getOpponentCode());
        Bet bet = Bet.newBuilder()
                .setInitiator(user)
                .setOpponent(opponent)
                .setFinishDate(draftBet.getFinishDate())
                .setDefinition(draftBet.getDefinition())
                .setWager(draftBet.getWager())
                .setInverseDefinition(draftBet.getInverseDefinition())
                .build();
        log.info("Сохраняем спор \n{}", bet);
        ResponseBet response = grpcStub.addBet(bet);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getBet();
        } else {
            log.error("Получена ошибка при попытке добавить новый спор по черновику: \n{}", draftBet);
            return null;
        }
    }

    public ResponseMessage setStatus(User user, long betId, UserBetStatus betStatus) {
        ChangeStatusBetMessage changeStatusBetMessage = ChangeStatusBetMessage.newBuilder()
                .setUser(user)
                .setNewStatus(betStatus)
                .setBet(Bet.newBuilder().setId(betId).build())
                .build();
        return grpcStub.changeStatusBet(changeStatusBetMessage);
    }

    public ResponseMessage showActiveBets(User user) {
        return grpcStub.getActiveBets(user);
    }

    public ResponseMessage showBet(User user, long id) {
        Bet bet = Bet.newBuilder().setId(id).setInitiator(user).build();
        return grpcStub.getBet(bet);
    }

    public ResponseBet showBetsWithFriend(User user, long friend_id, BetStatus betStatus) {
        Bet template = Bet.newBuilder()
                .setInitiator(user)
                .setOpponent(User.newBuilder().setId(friend_id).build())
                .setBetStatus(betStatus)
                .build();
        return grpcStub.getBetsByTemplate(template);
    }

    public void addArgument(User author, String text) {
        Argument argument = Argument.newBuilder()
                .setAuthor(author)
                .setText(text)
                .build();
        ResponseMessage response = grpcStub.addArgument(argument);
        if (Status.SUCCESS.equals(response.getStatus()) && response.hasBet()) {
            Bet bet = response.getBet();
            for (User user : List.of(bet.getInitiator(), bet.getOpponent())) {
                BetSendMessage msgToUser = new BetSendMessage(user.getChatId());
                if (bet.getInitiator().getUsername().equals(user.getUsername())
                        && bet.getInitiator().getCode() == user.getCode()) {
                    msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, true));
                } else {
                    msgToUser.setReplyMarkup(BetButtons.getBetButtons(bet, false));
                }
                msgToUser.setText(prettyPrinter.printBet(bet));
                botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ADD_ARGUMENT);
                botService.sendAndSaveBet(msgToUser, user, BotMessageOuterClass.BotMessageType.BET, bet);
            }
        }
    }
}
