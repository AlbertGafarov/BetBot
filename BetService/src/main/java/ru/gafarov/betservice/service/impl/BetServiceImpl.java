package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.BetStatusRule;
import ru.gafarov.betservice.entity.ChangeStatusBetRule;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.SubscribeService;
import ru.gafarov.betservice.service.UserService;

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
    private final List<BetStatusRule> betStatusRuleList;
    private final Converter converter;

    @Override
    public ProtoBet.ResponseBet save(ProtoBet.Bet protoBet) {

        Bet bet = new Bet();
        bet.setInitiator(userService.getUser(protoBet.getInitiator()));
        bet.setOpponent(userService.getUser(protoBet.getOpponent()));
        bet.setStatus(Status.ACTIVE);
        bet.setBetStatus(ProtoBet.BetStatus.OFFER);
        bet.setInitiatorBetStatus(ProtoBet.UserBetStatus.OFFERED);
        bet.setOpponentBetStatus(ProtoBet.UserBetStatus.OFFERED);
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
        return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.SUCCESS).setBet(protoBet).build();
    }

    public ProtoBet.ResponseMessage getActiveBets(UserOuterClass.User protoUser) {
        long userId = userService.getUser(protoUser).getId();
        List<Bet> activeBets = betRepository.getActiveBets(userId);
        List<ProtoBet.Bet> protoActiveBet = activeBets.stream().map(a -> {
            setNextStatuses(a);
            return converter.toProtoBet(a);
        }).collect(Collectors.toList());
        return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.SUCCESS).addAllBets(protoActiveBet).build();
    }

    @Override
    public ProtoBet.ResponseBet getBets(ProtoBet.Bet protoBet) {

        List<Bet> bets = betRepository.getBets(protoBet.getInitiator().getId()
                , protoBet.getOpponent().getId()
                , protoBet.getBetStatus().toString());
        if (bets.isEmpty()) {
            return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        } else {
            return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.SUCCESS)
                    .addAllBets(bets.stream().map(bet -> {
                        setNextStatuses(bet);
                        return converter.toProtoBet(bet);
                    }).collect(Collectors.toList())).build();
        }
    }

    @Override
    public ProtoBet.ResponseMessage showBet(ProtoBet.Bet protoBet) {
        Bet bet = betRepository.getBet(protoBet.getInitiator().getId(), protoBet.getId());
        if (bet != null) {
            setNextStatuses(bet);
            return ProtoBet.ResponseMessage.newBuilder().setBet(converter.toProtoBet(bet)).build();
        }
        return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
    }

    @Override
    public ProtoBet.ResponseMessage changeBetStatus(ProtoBet.ChangeStatusBetMessage protoChangeStatusBetMessage) {

        ProtoBet.Bet protoBet = protoChangeStatusBetMessage.getBet();
        log.debug("Спор, статус которого надо изменить {}", protoBet);
        log.debug("Пользователь который меняет статус спора {}", protoChangeStatusBetMessage.getUser());
        ProtoBet.UserBetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        log.debug("Новый статус спора {}", newBetStatus);
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            log.debug("Спор c id: {} найден в БД", protoBet.getId());
            Bet bet = optionalBet.get();
            BetRole userBetRole = bet.getInitiator().getUsername().equals(protoChangeStatusBetMessage.getUser().getUsername()) ? INITIATOR : OPPONENT;
            ProtoBet.UserBetStatus userStatus = userBetRole.equals(INITIATOR) ? bet.getInitiatorBetStatus() : bet.getOpponentBetStatus();
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
                    bet = betRepository.save(bet);
                    // Добавляем списки возможных следующих статусов
                    setBetStatus(bet);
                    setNextStatuses(bet);
                    return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.SUCCESS)
                            .setMessageForOpponent(rule.getMessageForOpponent())
                            .setMessageForInitiator(rule.getMessageForInitiator())
                            .setBet(converter.toProtoBet(bet)).build();
                } else {
                    log.warn("Изменение невозможно и не будет выполнено");
                    return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.NOT_SUCCESS)
                            .setBet(converter.toProtoBet(bet))
                            .setMessageForOpponent(Objects.requireNonNullElse(rule.getMessageForOpponent(), ""))
                            .setMessageForInitiator(Objects.requireNonNullElse(rule.getMessageForInitiator(), ""))
                            .build();
                }
            } else {
                log.error("Правило не найдено. Изменение статуса не будет выполнено");
                return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
            }
        }
        log.error("Спор с id: {} не найден", protoBet.getId());
        return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR)
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

    private void setBetStatus(Bet bet) {
        Optional<BetStatusRule> optional = betStatusRuleList.stream()
                .filter(a -> a.getInitiatorBetStatus().equals(bet.getInitiatorBetStatus()) &&
                        a.getOpponentBetStatus().equals(bet.getOpponentBetStatus())).findFirst();
        if (optional.isPresent()) {
            bet.setBetStatus(optional.get().getBetStatus());
            betRepository.save(bet);
        }
    }
}