package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;

public interface DraftBetService {

    Proto.ResponseMessage save(Proto.DraftBet protoBet);

    Proto.ResponseMessage setOpponentName(Proto.DraftBet request);

    Proto.ResponseMessage setOpponentCode(Proto.DraftBet request);

    Proto.ResponseMessage setDefinition(Proto.DraftBet request);

    Proto.ResponseMessage setWager(Proto.DraftBet request);

    Proto.ResponseMessage setFinishDate(Proto.DraftBet request);

    Proto.ResponseMessage getLastDraftBet(Proto.User request);

    Proto.ResponseMessage delete(Proto.DraftBet request);

    Proto.ResponseDraftBet getDraftBet(Proto.DraftBet request);
}