package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;

public interface DraftBetService {

    Proto.ResponseDraftBet save(Proto.DraftBet protoBet);

    Proto.ResponseDraftBet setOpponentName(Proto.DraftBet request);

    Proto.ResponseDraftBet setOpponentCode(Proto.DraftBet request);

    Proto.ResponseDraftBet setDefinition(Proto.DraftBet request);

    Proto.ResponseDraftBet setWager(Proto.DraftBet request);

    Proto.ResponseDraftBet setFinishDate(Proto.DraftBet request);

    Proto.ResponseDraftBet getLastDraftBet(Proto.User request);

    Proto.ResponseDraftBet delete(Proto.DraftBet request);

    Proto.ResponseDraftBet getDraftBet(Proto.DraftBet request);
}