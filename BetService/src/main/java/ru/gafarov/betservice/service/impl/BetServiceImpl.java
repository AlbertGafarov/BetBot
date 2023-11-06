package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.*;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.SubscribeService;
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
    private final ChangeStatusBetRuleRepository statusBetRepository;
    private final UserService userService;
    private final SubscribeService subscribeService;
    private final List<ChangeStatusBetRule> changeStatusBetRules;
    private final List<BetFinalStatusRule> betFinalStatusRuleList;
    private final Converter converter;

    @Override
    public Proto.ResponseBet save(Proto.Bet protoBet) {

        Bet bet = new Bet();
        bet.setInitiator(userService.getUser(protoBet.getInitiator()));
        bet.setOpponent(userService.getUser(protoBet.getOpponent()));
        bet.setStatus(Status.ACTIVE);
        bet.setInitiatorBetStatus(Proto.BetStatus.OFFERED);
        bet.setOpponentBetStatus(Proto.BetStatus.OFFERED);
        bet.setWager(protoBet.getWager());
        bet.setDefinition(protoBet.getDefinition());
        bet.setInverseDefinition(protoBet.getInverseDefinition());
        bet.setFinishDate(converter.toLocalDateTime(protoBet.getFinishDate()));
        bet = betRepository.save(bet);

        // Добавляем подписку, если ее нет
        subscribeService.checkAndPutForInitiator(bet);

        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getFinishDate()));
        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getFinishDate()));
        protoBet = converter.toProtoBet(bet);
        return Proto.ResponseBet.newBuilder().setStatus(Proto.Status.SUCCESS).setBet(protoBet).build();
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
        log.debug("Спор, статус которого надо изменить {}", protoBet);
        log.debug("Пользователь который меняет статус спора {}", protoChangeStatusBetMessage.getUser());
        Proto.BetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        log.debug("Новый статус спора {}", newBetStatus);
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            log.debug("Спор c id: {} найден в БД", protoBet.getId());
            Bet bet = optionalBet.get();
            BetRole userBetRole = bet.getInitiator().getUsername().equals(protoChangeStatusBetMessage.getUser().getUsername()) ? INITIATOR : OPPONENT;
            Proto.BetStatus userStatus = userBetRole.equals(INITIATOR) ? bet.getInitiatorBetStatus() : bet.getOpponentBetStatus();
            log.debug("Статус меняет {}: текущий: {}, новый: {}", userBetRole, userStatus, newBetStatus);
            ChangeStatusBetRule changeStatusBetWish = new ChangeStatusBetRule(userStatus, newBetStatus, userBetRole);
            ChangeStatusBetRule rule;
            Optional<ChangeStatusBetRule> statusBetOptional = changeStatusBetRules.stream().filter(a -> a.equals(changeStatusBetWish)).findFirst();
            if (statusBetOptional.isPresent()) {
                log.debug("Статусная модель найдена");
                rule = statusBetOptional.get();
                if (rule.isValid()) {
                    log.debug("Сообщение для оппонента: {}", rule.getMessageForOpponent());
                    log.debug("Сообщение для инициатора: {}", rule.getMessageForInitiator());

                    if (userBetRole.equals(INITIATOR)) {
                        bet.setInitiatorBetStatus(newBetStatus);
                        if (rule.getNewRivalBetStatus() != null) {
                            bet.setOpponentBetStatus(rule.getNewRivalBetStatus());
                        }
                    } else {
                        bet.setOpponentBetStatus(newBetStatus);
                        subscribeService.checkAndPutForOpponent(bet);

                        if (rule.getNewRivalBetStatus() != null) {
                            bet.setInitiatorBetStatus(rule.getNewRivalBetStatus());
                        }
                    }
                    bet.setUpdated(LocalDateTime.now());
                    bet = betRepository.save(bet);
                    // Добавляем списки возможных следующих статусов
                    setNextStatuses(bet);
                    setFinalStatus(bet);
                    return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.SUCCESS)
                            .setMessageForOpponent(rule.getMessageForOpponent())
                            .setMessageForInitiator(rule.getMessageForInitiator())
                            .setBet(converter.toProtoBet(bet)).build();
                } else {
                    log.warn("Изменение невозможно и не будет выполнено");
                    return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.NOT_SUCCESS)
                            .setBet(converter.toProtoBet(bet))
                            .setMessageForOpponent(Objects.requireNonNullElse(rule.getMessageForOpponent(), ""))
                            .setMessageForInitiator(Objects.requireNonNullElse(rule.getMessageForInitiator(), ""))
                            .build();
                }
            } else {
                log.error("Правило не найдено. Изменение статуса не будет выполнено");
                return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR).build();
            }
        }
        log.error("Спор с id: {} не найден", protoBet.getId());
        return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR)
                .setMessageForOpponent("Спор с id: " + protoBet.getId() + " не найден")
                .build();
    }

    private void setNextStatuses(Bet bet) {
        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getFinishDate()));
        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getFinishDate()));
    }

    private void setFinalStatus(Bet bet) {
        if (bet.getNextOpponentBetStatusList().isEmpty() && bet.getNextInitiatorBetStatusList().isEmpty()) {
            bet.setStatus(Status.CLOSED);
            bet.setUpdated(LocalDateTime.now());
            betRepository.save(bet);
        }
        Optional<BetFinalStatusRule> optionalBetFinalStatusRule = betFinalStatusRuleList.stream().filter(a -> a.getInitiatorBetStatus().equals(bet.getInitiatorBetStatus()) && 
        a.getOpponentBetStatus().equals(bet.getOpponentBetStatus())).findFirst();

        if (optionalBetFinalStatusRule.isPresent()) {
            BetFinalStatusRule rule = optionalBetFinalStatusRule.get();
            bet.setStatus(rule.getBetFinalStatus());
            bet.setUpdated(LocalDateTime.now());
            betRepository.save(bet);
        }
    }
}