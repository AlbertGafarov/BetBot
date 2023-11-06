package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.*;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class BetService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final UserService userService;

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

    public ResponseMessage setStatus(User user, long betId, BetStatus betStatus) {
        ChangeStatusBetMessage changeStatusBetMessage = ChangeStatusBetMessage.newBuilder()
                .setUser(user)
                .setNewStatus(betStatus)
                .setBet(Bet.newBuilder().setId(betId).build())
                .build();
        return grpcStub.changeStatusBet(changeStatusBetMessage);
    }

    public ResponseMessage  showActiveBets(User user) {
        return grpcStub.getActiveBets(user);
    }

    public ResponseMessage showBet(User user, long id) {
        Bet bet = Bet.newBuilder().setId(id).setInitiator(user).build();
        return grpcStub.getBet(bet);
    }

}
