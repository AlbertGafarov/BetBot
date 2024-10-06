package ru.gafarov.betservice.service;

import org.springframework.transaction.annotation.Transactional;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.Bet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Transactional
public interface BetService {

    ProtoBet.ResponseBet save(ProtoBet.Bet protoBet);

    ProtoBet.ResponseMessage showBet(ProtoBet.Bet protoBet);

    ProtoBet.ResponseMessage changeBetStatus(ProtoBet.ChangeStatusBetMessage protoChangeStatusBetMessage);

    ProtoBet.ResponseMessage getActiveBets(UserOuterClass.User protoUser);

    ProtoBet.ResponseBet getBets(ProtoBet.Bet request);

    Bet getBet(long userId, long betId);

    ProtoBet.ResponseMessage showBet(Long userId, Long id) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    List<Bet> getExpiredBets();
}