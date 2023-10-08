package ru.gafarov.betservice.service.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.*;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRulesRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.gafarov.betservice.model.BetRole.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {

    private final BetRepository betRepository;
    private final ChangeStatusBetRulesRepository statusBetRepository;
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

        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString(), bet.getOpponentBetStatus().toString()));
        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString(), bet.getInitiatorBetStatus().toString()));
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
        log.info("Спор, статус которого надо изменить {}", protoBet);
        Proto.User protoUser = protoChangeStatusBetMessage.getUser();
        log.info("Пользователь который меняет статус спора {}", protoUser);
        Proto.BetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        log.info("Новый статус спора {}", newBetStatus);
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            log.info("Спор c id: {} найден в БД", protoBet.getId());
            Bet bet = optionalBet.get();
            User initiator = bet.getInitiator();
            Proto.BetStatus initiatorBetStatus = bet.getInitiatorBetStatus();
            log.info("Инициатор: {}. Его статус: {}", initiator, initiatorBetStatus);
            User opponent = bet.getOpponent();
            Proto.BetStatus opponentBetStatus = bet.getOpponentBetStatus();
            log.info("Оппонент: {}. Его статус: {}", opponent, opponentBetStatus);

            if (initiator.getUsername().equals(protoUser.getUsername())) {
                log.info("Статус меняет initiator: текущий: {}, новый: {}", initiatorBetStatus, newBetStatus);
                ChangeStatusBetRules statusBet = new ChangeStatusBetRules(initiatorBetStatus, newBetStatus, INITIATOR);
                ChangeStatusBetRules finalStatusBet = statusBet;
                Optional<ChangeStatusBetRules> statusBetOptional = changeStatusBetRules.stream().filter(a -> a.equals(finalStatusBet)).findFirst();
                if (statusBetOptional.isPresent()) {
                    log.info("Статусная модель найдена");
                    statusBet = statusBetOptional.get();
                    if (statusBet.isValid()) {
                        log.info("Сообщение для оппонента: {}", statusBet.getMessageForOpponent());
                        log.info("Сообщение для инициатора: {}", statusBet.getMessageForInitiator());
                        bet.setInitiatorBetStatus(newBetStatus);
                        if (statusBet.getNewRivalBetStatus() != null) {
                            bet.setOpponentBetStatus(statusBet.getNewRivalBetStatus());
                        }
                        bet.setUpdated(LocalDateTime.now());
                        bet.setStatus(Status.ACTIVE);
                        bet = betRepository.save(bet);
                        // Добавляем списки возможных следующих статусов
                        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString(), bet.getInitiatorBetStatus().toString()));
                        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString(), bet.getOpponentBetStatus().toString()));
                        log.info("Возможные статусы для инициатора: {}", bet.getNextInitiatorBetStatusList());
                        log.info("Возможные статусы для оппонента: {}", bet.getNextOpponentBetStatusList());
                        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.SUCCESS)
                                .setMessageForOpponent(statusBet.getMessageForOpponent())
                                .setMessageForInitiator(statusBet.getMessageForInitiator())
                                .setBet(converter.toProtoBet(bet)).build();
                    } else {
                        log.info("Изменение невозможно и не будет выполнено");
                        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.NOT_SUCCESS)
                                .setBet(converter.toProtoBet(bet))
                                .setMessageForOpponent(Objects.requireNonNullElse(statusBet.getMessageForOpponent(), ""))
                                .setMessageForInitiator(Objects.requireNonNullElse(statusBet.getMessageForInitiator(), ""))
                                .build();
                    }
                }
            } else if (opponent.getUsername().equals(protoUser.getUsername())) {
                log.info("Статус меняет opponent: текущий: {}, новый: {}", opponentBetStatus, newBetStatus);
                ChangeStatusBetRules statusBet = new ChangeStatusBetRules(opponentBetStatus, newBetStatus, OPPONENT);
                log.info("Замена статуса: {}", statusBet);
                ChangeStatusBetRules finalStatusBet = statusBet;
                Optional<ChangeStatusBetRules> statusBetOptional = changeStatusBetRules.stream().filter(a -> a.equals(finalStatusBet)).findFirst();
                if (statusBetOptional.isPresent()) {
                    log.info("Статусная модель найдена");
                    statusBet = statusBetOptional.get();
                    if (statusBet.isValid()) {
                        log.info("Сообщение для оппонента: {}", statusBet.getMessageForOpponent());
                        log.info("Сообщение для инициатора: {}", statusBet.getMessageForInitiator());
                        bet.setOpponentBetStatus(newBetStatus);
                        if (statusBet.getNewRivalBetStatus() != null) {
                            bet.setInitiatorBetStatus(statusBet.getNewRivalBetStatus());
                        }
                        bet.setUpdated(LocalDateTime.now());
                        bet.setStatus(Status.ACTIVE);
                        bet = betRepository.save(bet);
                        // Добавляем списки возможных следующих статусов
                        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString(), bet.getInitiatorBetStatus().toString()));
                        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString(), bet.getOpponentBetStatus().toString()));
                        log.info("Возможные статусы для инициатора: {}", bet.getNextInitiatorBetStatusList());
                        log.info("Возможные статусы для оппонента: {}", bet.getNextOpponentBetStatusList());
                        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.SUCCESS)
                                .setMessageForOpponent(statusBet.getMessageForOpponent())
                                .setMessageForInitiator(statusBet.getMessageForInitiator())
                                .setBet(converter.toProtoBet(bet)).build();
                    } else {
                        log.info("Изменение невозможно и не будет выполнено");
                        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.NOT_SUCCESS)
                                .setBet(converter.toProtoBet(bet))
                                .setMessageForOpponent(statusBet.getMessageForOpponent())
                                .setMessageForInitiator(Objects.requireNonNullElse(statusBet.getMessageForInitiator(), ""))
                                .build();
                    }
                } else {
                    log.error("Такое статусное изменение не найдено");
                    return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                            .setBet(converter.toProtoBet(bet))
                            .setMessageForOpponent("Такое статусное изменение не найдено")
                            .build();
                }
            } else {
                return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.ERROR)
                        .setMessageForOpponent("You don't have bet with id: " + protoBet.getId())
                        .build();
            }
        }
        return null;
    }
}