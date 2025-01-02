package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.converter.BetConverter;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.BetStatusRule;
import ru.gafarov.betservice.entity.ChangeStatusBetRule;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.BetRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.service.SubscribeService;
import ru.gafarov.betservice.service.UserService;
import ru.gafarov.betservice.transformer.BetTransformer;
import ru.gafarov.betservice.utils.CryptoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    private final List<BetStatusRule> betStatusRuleList;
    private final Converter converter;
    private final BetTransformer betTransformer;
    private final MessageWithKeyService messageWithKeyService;

    @Override
    public ProtoBet.ResponseBet save(ProtoBet.Bet protoBet) {

        Bet bet = new Bet();
        bet.setInitiator(userService.getUser(protoBet.getInitiator()));
        bet.setOpponent(userService.getUser(protoBet.getOpponent()));
        bet.setStatus(Status.ACTIVE);
        bet.setBetStatus(ProtoBet.BetStatus.OFFER);
        bet.setInitiatorBetStatus(ProtoBet.UserBetStatus.OFFERED);
        bet.setOpponentBetStatus(ProtoBet.UserBetStatus.OFFERED);
        //Если хотя бы у одного из участников включено шифрование, то необходимо зашифровать спор
        if (protoBet.getInitiator().getEncryptionEnabled() || protoBet.getOpponent().getEncryptionEnabled()) {
            String pairSecret = messageWithKeyService.getPairSecret(bet.getInitiator(), bet.getOpponent());
            bet.setWager(CryptoUtils.encrypt(protoBet.getWager(), pairSecret));
            bet.setDefinition(CryptoUtils.encrypt(protoBet.getDefinition(), pairSecret));
            bet.setEncrypted(true);
        } else {
            bet.setWager(protoBet.getWager());
            bet.setDefinition(protoBet.getDefinition());
        }
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

        protoBet = betTransformer.getDecryptedProtoBet(protoBet.getInitiator().getId(), bet);
        return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.SUCCESS)
                .setBet(protoBet)
                .build();
    }

    public ProtoBet.ResponseMessage getActiveBets(UserOuterClass.User protoUser) {
        long userId = userService.getUser(protoUser).getId();
        List<Bet> activeBets = betRepository.getActiveBets(userId);
        List<ProtoBet.Bet> protoActiveBet = activeBets.stream().map(a -> {
            betTransformer.setNextStatuses(a);
            return betTransformer.getDecryptedProtoBet(protoUser.getId(), a);
        }).collect(Collectors.toList());
        return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.SUCCESS).addAllBets(protoActiveBet).build();
    }

    @Override
    public ProtoBet.ResponseBet getBets(ProtoBet.Bet protoBet) {

        log.debug("Шаблон для поиска споров\ninitiator: {}\nopponent: {}\nstatus: {}"
                , protoBet.getInitiator().getId(), protoBet.getOpponent().getId(), protoBet.getBetStatus());

        List<Bet> bets = betRepository.getBets(protoBet.getInitiator().getId()
                , protoBet.getOpponent().getId()
                , protoBet.getBetStatus().toString());

        log.debug("Список споров, удовлетворяющих шаблону: {}", bets.stream().map(a ->
                        "\nid: " + a.getId() + " isEncrypted: " + a.isEncrypted())
                .collect(Collectors.joining(" ")));

        if (bets.isEmpty()) {
            return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        } else {
            return ProtoBet.ResponseBet.newBuilder().setStatus(Rs.Status.SUCCESS)
                    .addAllBets(bets.stream().map(bet -> {
                        // здесь мы используем id инициатора, потому что знаем, что при заполнении шаблона спора пользователь,
                        // который получает список, записывается в качестве инициатора
                        return betTransformer.getDecryptedProtoBet(protoBet.getInitiator().getId(), bet);
                    }).collect(Collectors.toList())).build();
        }
    }

    @Override
    public Bet getBet(long userId, long betId) {
        return betRepository.getBet(userId, betId);
    }

    @Override
    public ProtoBet.ResponseMessage showBet(Long userId, Long id) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Bet bet = betRepository.getBet(userId, id);
        if (bet != null) {
            ProtoBet.Bet protoBet = betTransformer.getDecryptedProtoBet(userId, bet);
            return ProtoBet.ResponseMessage.newBuilder().setBet(protoBet).build();
        }
        log.error("Не найден спор с id: {} user_id: {}", id, userId);
        return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
    }

    @Override
    public List<Bet> getExpiredBets() {
        return betRepository.getExpiredBets(LocalDateTime.now());
    }

    @Override
    public ProtoBet.ResponseMessage showBet(ProtoBet.Bet protoBet) {
        try {
            return showBet(protoBet.getInitiator().getId(), protoBet.getId());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProtoBet.ResponseMessage changeBetStatus(ProtoBet.ChangeStatusBetMessage protoChangeStatusBetMessage) {

        ProtoBet.Bet protoBet = protoChangeStatusBetMessage.getBet();
        log.debug("Спор, статус которого надо изменить {}", protoBet);
        UserOuterClass.User changerUser = protoChangeStatusBetMessage.getUser();
        log.debug("Пользователь который меняет статус спора {}", changerUser);
        ProtoBet.UserBetStatus newBetStatus = protoChangeStatusBetMessage.getNewStatus();
        log.debug("Новый статус спора {}", newBetStatus);
        Optional<Bet> optionalBet = betRepository.findById(protoBet.getId());
        if (optionalBet.isPresent()) {
            log.debug("Спор c id: {} найден в БД", protoBet.getId());
            Bet bet = optionalBet.get();
            BetRole userBetRole = bet.getInitiator().getUsername().equals(changerUser.getUsername()) ? INITIATOR : OPPONENT;
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
                    betTransformer.setNextStatuses(bet);
                    return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.SUCCESS)
                            .setMessageForOpponent(rule.getMessageForOpponent())
                            .setMessageForInitiator(rule.getMessageForInitiator())
                            .setBet(betTransformer.getDecryptedProtoBet(changerUser.getId(), bet)).build();
                } else {
                    log.warn("Изменение невозможно и не будет выполнено");
                    return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.NOT_SUCCESS)
                            .setBet(BetConverter.toProtoBetBuilder(bet))
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

    private void setBetStatus(Bet bet) {
        Optional<BetStatusRule> optional = betStatusRuleList.stream()
                .filter(a -> a.getInitiatorBetStatus().equals(bet.getInitiatorBetStatus()) &&
                        a.getOpponentBetStatus().equals(bet.getOpponentBetStatus())).findFirst();
        if (optional.isPresent()) {
            // Если спор без вознаграждения, то статус ожидания оплаты пропускается
            ProtoBet.BetStatus newBetStatus = bet.getWager().isEmpty() &&
                    optional.get().getBetStatus().equals(ProtoBet.BetStatus.WAIT_WAGER_PAY) ?
                    ProtoBet.BetStatus.CLOSED : optional.get().getBetStatus();
            bet.setBetStatus(newBetStatus);
            betRepository.save(bet);
        }
    }
}