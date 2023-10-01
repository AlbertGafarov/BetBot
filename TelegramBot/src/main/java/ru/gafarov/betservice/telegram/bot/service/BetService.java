package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class BetService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final UserService userService;

    public Proto.Bet addBet(Proto.DraftBet draftBet, Proto.User user) {
        Proto.User opponent = userService.getUser(draftBet.getOpponentName(), draftBet.getOpponentCode());
        Proto.Bet bet = Proto.Bet.newBuilder()
                .setInitiator(user)
                .setOpponent(opponent)
                .setFinishDate(draftBet.getFinishDate())
                .setDefinition(draftBet.getDefinition())
                .setWager(draftBet.getWager())
                .build();
        log.info("Сохраняем спор \n{}", bet);
        return grpcStub.addBet(bet).getBet();
    }

    public Proto.Bet setStatus(Proto.User user, long betId, Proto.BetStatus betStatus) {
        Proto.ChangeStatusBetMessage changeStatusBetMessage = Proto.ChangeStatusBetMessage.newBuilder()
                .setUser(user)
                .setNewStatus(betStatus)
                .setBet(Proto.Bet.newBuilder().setId(betId).build())
                .build();
        return grpcStub.changeStatusBet(changeStatusBetMessage).getBet();
    }
}
