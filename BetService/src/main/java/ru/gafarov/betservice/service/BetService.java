package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;

public interface BetService {

    Proto.ResponseMessage save(Proto.Bet protoBet);

    Proto.ResponseMessage showBet(Proto.Bet protoBet);

    Proto.ResponseMessage changeBetStatus(Proto.ChangeStatusBetMessage protoChangeStatusBetMessage);
}