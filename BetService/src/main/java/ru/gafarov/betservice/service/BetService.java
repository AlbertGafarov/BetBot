package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;

public interface BetService {

    ProtoBet.ResponseBet save(ProtoBet.Bet protoBet);

    ProtoBet.ResponseMessage showBet(ProtoBet.Bet protoBet);

    ProtoBet.ResponseMessage changeBetStatus(ProtoBet.ChangeStatusBetMessage protoChangeStatusBetMessage);

    ProtoBet.ResponseMessage getActiveBets(UserOuterClass.User protoUser);

    ProtoBet.ResponseBet getBets(ProtoBet.Bet request);
}