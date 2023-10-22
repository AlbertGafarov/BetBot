package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.Bet;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.model.ChangeStatusBetRules;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRulesRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.gafarov.betservice.model.BetRole.INITIATOR;
import static ru.gafarov.betservice.model.BetRole.OPPONENT;

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

    public Proto.ResponseMessage getActiveBets(Proto.User protoUser) {
        long userId = userService.getUser(protoUser).getId();
        List<Bet> activeBets = betRepository.getActiveBets(userId);
        List<Proto.Bet> protoActiveBet = activeBets.stream().map(a -> {
            setNextStatuses(a);
            return converter.toProtoBet(a);
        }).collect(Collectors.toList());
        return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.SUCCESS).addAllBets(protoActiveBet).build();
    }

    @Override
    public Proto.ResponseMessage showBet(Proto.Bet protoBet) {
        Bet bet = betRepository.getBet(protoBet.getInitiator().getId(), protoBet.getId());
        if (bet != null) {
            setNextStatuses(bet);
            return Proto.ResponseMessage.newBuilder().setBet(converter.toProtoBet(bet)).build();
        }
        return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR).build();
    }

    @Override
    public Proto.ResponseMessage changeBetStatus(Proto.ChangeStatusBetMessage protoChangeStatusBetMessage) {

        Proto.Bet protoBet = protoChangeStatusBetMessage.getBet();
        log.info("Спор, статус которого надо изменить {}", protoBet);
        log.info("Пользователь который меняет статус спора {}", protoChangeStatusBetMessage.getUser());
        Proto.BetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        log.info("Новый статус спора {}", newBetStatus);
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            log.info("Спор c id: {} найден в БД", protoBet.getId());
            Bet bet = optionalBet.get();
            BetRole userBetRole = bet.getInitiator().getUsername().equals(protoChangeStatusBetMessage.getUser().getUsername()) ? INITIATOR : OPPONENT;
            Proto.BetStatus userStatus = userBetRole.equals(INITIATOR) ? bet.getInitiatorBetStatus() : bet.getOpponentBetStatus();
            log.info("Статус меняет {}: текущий: {}, новый: {}", userBetRole, userStatus, newBetStatus);
            ChangeStatusBetRules statusBet = new ChangeStatusBetRules(userStatus, newBetStatus, userBetRole);
            ChangeStatusBetRules finalStatusBet = statusBet;
            Optional<ChangeStatusBetRules> statusBetOptional = changeStatusBetRules.stream().filter(a -> a.equals(finalStatusBet)).findFirst();
            if (statusBetOptional.isPresent()) {
                log.info("Статусная модель найдена");
                statusBet = statusBetOptional.get();
                if (statusBet.isValid()) {
                    log.info("Сообщение для оппонента: {}", statusBet.getMessageForOpponent());
                    log.info("Сообщение для инициатора: {}", statusBet.getMessageForInitiator());

                    if (userBetRole.equals(INITIATOR)) {
                        bet.setInitiatorBetStatus(newBetStatus);
                        if (statusBet.getNewRivalBetStatus() != null) {
                            bet.setOpponentBetStatus(statusBet.getNewRivalBetStatus());
                        }
                    } else {
                        bet.setOpponentBetStatus(newBetStatus);
                        if (statusBet.getNewRivalBetStatus() != null) {
                            bet.setInitiatorBetStatus(statusBet.getNewRivalBetStatus());
                        }
                    }
                    bet.setUpdated(LocalDateTime.now());
                    bet = betRepository.save(bet);
                    // Добавляем списки возможных следующих статусов
                    setNextStatuses(bet);
                    setFinalStatus(bet);
                    return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.SUCCESS)
                            .setMessageForOpponent(statusBet.getMessageForOpponent())
                            .setMessageForInitiator(statusBet.getMessageForInitiator())
                            .setBet(converter.toProtoBet(bet)).build();
                } else {
                    log.info("Изменение невозможно и не будет выполнено");
                    return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.NOT_SUCCESS)
                            .setBet(converter.toProtoBet(bet))
                            .setMessageForOpponent(Objects.requireNonNullElse(statusBet.getMessageForOpponent(), ""))
                            .setMessageForInitiator(Objects.requireNonNullElse(statusBet.getMessageForInitiator(), ""))
                            .build();
                }
            } else {
                log.error("Изменение не найдено в БД и не будет выполнено");
                return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR)
                        .setMessageForOpponent("Изменение не найдено в БД и не будет выполнено")
                        .build();
            }
        }
        log.error("Спор с id: {} не найден", protoBet.getId());
        return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR)
                .setMessageForOpponent("Спор с id: " + protoBet.getId() + " не найден")
                .build();
    }

    private void setNextStatuses(Bet bet) {
        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString(), bet.getInitiatorBetStatus().toString()));
        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString(), bet.getOpponentBetStatus().toString()));
        log.info("Возможные статусы для инициатора: {}", bet.getNextInitiatorBetStatusList());
        log.info("Возможные статусы для оппонента: {}", bet.getNextOpponentBetStatusList());

    }

    private void setFinalStatus(Bet bet) {
        if (bet.getNextOpponentBetStatusList().isEmpty() && bet.getNextInitiatorBetStatusList().isEmpty()) {
            bet.setStatus(Status.NOT_ACTIVE);
            bet.setUpdated(LocalDateTime.now());
            betRepository.save(bet);
        }
    }
}