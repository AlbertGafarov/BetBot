package ru.gafarov.betservice.service.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.Bet;
import ru.gafarov.betservice.model.ChangeStatusBetRules;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.model.User;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {

    private final BetRepository betRepository;
    private final UserService userService;
    private final List<ChangeStatusBetRules> changeStatusBetRules;
    private final Converter converter;

    @Override
    public Proto.ResponseMessage save(Proto.Bet protoBet) {

        Bet bet = new Bet();
        bet.setCreated(LocalDateTime.now());
        bet.setInitiator(userService.getUser(protoBet.getInitiator()));
        bet.setOpponent(userService.getUser(protoBet.getOpponent()));
        bet.setStatus(Status.ACTIVE);
        bet.setInitiatorBetStatus(Proto.BetStatus.OFFERED);
        bet.setOpponentBetStatus(Proto.BetStatus.OFFERED);
        bet.setWager(protoBet.getWager());
        bet.setDefinition(protoBet.getDefinition());
        bet.setFinishDate(converter.toLocalDateTime(protoBet.getFinishDate()));
        bet = betRepository.save(bet);
        protoBet = converter.toProtoBet(bet);
        return Proto.ResponseMessage.newBuilder().setBet(protoBet).build();

    }

    @Override
    public Proto.ResponseMessage showBet(Proto.Bet protoBet) {
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            Bet bet = optionalBet.get();
            Instant instant = bet.getFinishDate().toInstant((ZoneOffset) ZoneOffset.systemDefault());
            return Proto.ResponseMessage.newBuilder().setBet(

                    Proto.Bet.newBuilder(protoBet)
                            .setDefinition(bet.getDefinition())
                            .setInitiator(Proto.User.newBuilder().setUsername(bet.getInitiator().getUsername()).build())
                            .setOpponent(Proto.User.newBuilder().setUsername(bet.getOpponent().getUsername()).build())
                            .setFinishDate(Timestamp.newBuilder()
                                    .setSeconds(instant.getEpochSecond())
                                    .setNanos(instant.getNano())
                                    .build())
                            .setInitiatorStatus(bet.getInitiatorBetStatus())
                            .setOpponentStatus(bet.getOpponentBetStatus())
                            .build()).build();
        }
        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR).build();
    }

    @Override
    public Proto.ResponseMessage changeBetStatus(Proto.ChangeStatusBetMessage protoChangeStatusBetMessage) {

        Proto.Bet protoBet = protoChangeStatusBetMessage.getBet();
        Proto.User protoUser = protoChangeStatusBetMessage.getUser();
        Proto.BetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            Bet bet = optionalBet.get();
            User initiator = bet.getInitiator();
            User opponent = bet.getOpponent();

            Proto.BetStatus initiatorBetStatus = bet.getInitiatorBetStatus();
            Proto.BetStatus opponentBetStatus = bet.getOpponentBetStatus();

            if (initiator.getUsername().equals(protoUser.getUsername())) {
                log.info("Статус меняет initiator");
                ChangeStatusBetRules statusBet = new ChangeStatusBetRules(initiatorBetStatus, newBetStatus);
                if (initiatorBetStatus.equals(Proto.BetStatus.OFFERED)) {
                    log.info("You initiator and status OFFERED. You can only cancel the bet");
                    return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                            .setMessage("You initiator and status OFFERED. You can only cancel the bet").build();
                }
                if (changeStatusBetRules.contains(statusBet)) {
                    if (statusBet.getNewBetStatus().equals(Proto.BetStatus.CANCEL)) {
                        bet.setOpponentBetStatus(Proto.BetStatus.CANCEL);
                        bet.setStatus(Status.NOT_ACTIVE);
                    }
                    bet.setInitiatorBetStatus(newBetStatus);
                    bet.setUpdated(LocalDateTime.now());
                    betRepository.save(bet);
                    Instant instant = bet.getFinishDate().toInstant((ZoneOffset) ZoneOffset.systemDefault());
                    return Proto.ResponseMessage.newBuilder().setBet(Proto.Bet.newBuilder(protoBet)
                            .setDefinition(bet.getDefinition())
                            .setInitiator(Proto.User.newBuilder().setUsername(bet.getInitiator().getUsername()).build())
                            .setOpponent(Proto.User.newBuilder().setUsername(bet.getOpponent().getUsername()).build())
                            .setFinishDate(Timestamp.newBuilder()
                                    .setSeconds(instant.getEpochSecond())
                                    .setNanos(instant.getNano())
                                    .build())
                            .setInitiatorStatus(bet.getInitiatorBetStatus())
                            .setOpponentStatus(bet.getOpponentBetStatus())
                            .build()).build();

                } else {
                    String message = "";
                    Optional<String> opt = changeStatusBetRules.stream().filter(s -> s.getCurrentBetStatus()
                            .equals(statusBet.getCurrentBetStatus()))
                            .map(ChangeStatusBetRules::getMessage)
                            .findFirst();
                    if (opt.isPresent()) message = opt.get();
                    return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                            .setMessage(message).build();
                }

            } else if (opponent.getUsername().equals(protoUser.getUsername())) {
                log.info("Статус меняет opponent");
                ChangeStatusBetRules statusBet = new ChangeStatusBetRules(opponentBetStatus, newBetStatus);
                if (changeStatusBetRules.contains(statusBet)) {
                    log.info("Статусная модель найдена");
                    if (statusBet.getNewBetStatus().equals(Proto.BetStatus.CANCEL)) {
                        log.info("Спор отклонен по желанию оппонента");
                        bet.setInitiatorBetStatus(Proto.BetStatus.CANCEL);
                        bet.setStatus(Status.NOT_ACTIVE);
                    }
                    if (statusBet.getNewBetStatus().equals(Proto.BetStatus.ACCEPTED)) {
                        if (LocalDateTime.now().isAfter(bet.getFinishDate())) {
                            bet.setInitiatorBetStatus(Proto.BetStatus.CANCEL);
                            bet.setOpponentBetStatus(Proto.BetStatus.CANCEL);
                            bet.setStatus(Status.NOT_ACTIVE);
                            return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                                    .setMessage("Time is up. The bet will change status on CANCEL").build();
                        }
                        bet.setInitiatorBetStatus(Proto.BetStatus.ACCEPTED);
                        bet.setStatus(Status.ACTIVE);
                    }
                    bet.setOpponentBetStatus(newBetStatus);
                    bet.setUpdated(LocalDateTime.now());
                    betRepository.save(bet);
                    Instant instant = bet.getFinishDate().toInstant((ZoneOffset) ZoneOffset.systemDefault());
                    return Proto.ResponseMessage.newBuilder().setBet(Proto.Bet.newBuilder(protoBet)
                            .setDefinition(bet.getDefinition())
                            .setInitiator(Proto.User.newBuilder().setUsername(bet.getInitiator().getUsername()).build())
                            .setOpponent(Proto.User.newBuilder().setUsername(bet.getOpponent().getUsername()).build())
                            .setFinishDate(Timestamp.newBuilder()
                                    .setSeconds(instant.getEpochSecond())
                                    .setNanos(instant.getNano())
                                    .build())
                            .setInitiatorStatus(bet.getInitiatorBetStatus())
                            .setOpponentStatus(bet.getOpponentBetStatus())
                            .build()).build();
                } else {
                    return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                            .setMessage(statusBet.getMessage()).build();
                }
            } else {
                return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                        .setMessage("You don't have bet with id: " + protoBet.getId()).build();
            }
        }
        return null;
    }
}