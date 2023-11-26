package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;

public interface DraftBetService {

    DrBet.ResponseDraftBet save(DrBet.DraftBet protoBet);

    DrBet.ResponseDraftBet setOpponentName(DrBet.DraftBet request);

    DrBet.ResponseDraftBet setOpponentCodeAndName(DrBet.DraftBet request);

    DrBet.ResponseDraftBet setDefinition(DrBet.DraftBet request);

    DrBet.ResponseDraftBet setWager(DrBet.DraftBet request);

    DrBet.ResponseDraftBet setFinishDate(DrBet.DraftBet request);

    DrBet.ResponseDraftBet getLastDraftBet(UserOuterClass.User request);

    DrBet.ResponseDraftBet delete(DrBet.DraftBet request);

    DrBet.ResponseDraftBet getDraftBet(DrBet.DraftBet request);
}