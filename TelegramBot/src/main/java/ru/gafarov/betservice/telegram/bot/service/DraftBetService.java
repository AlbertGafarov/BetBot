package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftBetService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;

    public Proto.DraftBet saveDraftBet(Proto.DraftBet draftBet) {
        log.info("Сохраняем черновик запроса \n{}", draftBet.toString());
        return grpcStub.addDraftBet(draftBet).getDraftBet();
    }

    public Proto.DraftBet setOpponentName(Proto.DraftBet draftBet) {
        log.info("Сохраняем имя оппонента в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setOpponentName(draftBet).getDraftBet();
    }


    public Proto.DraftBet setOpponentCode(Proto.DraftBet draftBet) {
        log.info("Сохраняем код оппонента в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setOpponentCode(draftBet).getDraftBet();
    }

    public Proto.DraftBet setDefinition(Proto.DraftBet draftBet) {
        log.info("Сохраняем суть в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setDefinition(draftBet).getDraftBet();
    }

    public Proto.DraftBet setWager(Proto.DraftBet draftBet) {
        log.info("Сохраняем вознаграждение в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setWager(draftBet).getDraftBet();
    }

    public Proto.DraftBet setDaysToFinish(Proto.DraftBet draftBet) {
        log.info("Сохраняем дату завершения спора в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setFinishDate(draftBet).getDraftBet();
    }
}
