package ru.gafarov.betservice.transformer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.converter.ArgumentConverter;
import ru.gafarov.betservice.converter.BetConverter;
import ru.gafarov.betservice.entity.Argument;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;
import ru.gafarov.betservice.service.MessageWithKeyService;
import ru.gafarov.betservice.transformer.BetTransformer;
import ru.gafarov.betservice.utils.CryptoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.gafarov.betservice.model.BetRole.INITIATOR;
import static ru.gafarov.betservice.model.BetRole.OPPONENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetTransformerImpl implements BetTransformer {
    private final MessageWithKeyService messageWithKeyService;
    private final ChangeStatusBetRuleRepository statusBetRepository;

    @Override
    public ProtoBet.Bet getDecryptedProtoBet(Long userId, Bet bet) {
        // Получаем юзера для которого формируется ответ, а также юзера оппонента по спору. Это нужно для того, чтобы получить подписку,
        // а из нее получить парный ключ, зашифрованный ключом юзера, а не оппонента.
        User subscriber;
        User subscribed;
        String pairSecret = null;
        if (Objects.equals(bet.getInitiator().getId(), userId)) {
            subscriber = bet.getInitiator();
            subscribed = bet.getOpponent();
        } else {
            subscriber = bet.getOpponent();
            subscribed = bet.getInitiator();
        }
        setNextStatuses(bet);
        ProtoBet.Bet.Builder builder = BetConverter.toProtoBetBuilder(bet);
        if (bet.isEncrypted()) {
            log.debug("isEncrypted");
            pairSecret = messageWithKeyService.getPairSecret(subscriber, subscribed);
            builder.setDefinition(CryptoUtils.decryptText(bet.getDefinition(), pairSecret));
            if (bet.getWager() != null) {
                builder.setWager(CryptoUtils.decryptText(bet.getWager(), pairSecret));
            }
        } else {
            builder.setDefinition(bet.getDefinition());
            if (bet.getWager() != null) {
                builder.setWager(bet.getWager());
            }
        }

        if (bet.getArguments() != null) {
            List<ProtoBet.Argument> decryptedArgumentList = new ArrayList<>();
            for (Argument argument : bet.getArguments()) {
                ProtoBet.Argument.Builder protoArgumentBuilder = ArgumentConverter.toProtoArgumentBuilder(argument);
                if (argument.isEncrypted()) {
                    if (pairSecret == null) {
                        pairSecret = messageWithKeyService.getPairSecret(subscriber, subscribed);
                    }
                    protoArgumentBuilder.setText(CryptoUtils.decryptText(argument.getText()
                            , pairSecret));
                } else {
                    protoArgumentBuilder.setText(argument.getText()).build();
                }
                decryptedArgumentList.add(protoArgumentBuilder.build());
            }
            builder.addAllArguments(decryptedArgumentList);
        }

        return builder.build();
    }

    @Override
    public void setNextStatuses(Bet bet) {
        bet.setNextInitiatorBetStatusList(statusBetRepository.getNextStatuses(INITIATOR.toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getFinishDate()));
        bet.setNextOpponentBetStatusList(statusBetRepository.getNextStatuses(OPPONENT.toString()
                , bet.getOpponentBetStatus().toString()
                , bet.getInitiatorBetStatus().toString()
                , bet.getFinishDate()));
        // Если спор без вознаграждения, то статусы оплаты и ожидания оплаты не отображаются
        if (bet.getWager().isEmpty()) {
            if (bet.getNextInitiatorBetStatusList().equals(List.of(ProtoBet.UserBetStatus.WAGERPAID)) ||
                    bet.getNextInitiatorBetStatusList().equals(List.of(ProtoBet.UserBetStatus.WAGERRECIEVED))) {
                bet.getNextInitiatorBetStatusList().clear();
            }
            if (bet.getNextOpponentBetStatusList().equals(List.of(ProtoBet.UserBetStatus.WAGERPAID)) ||
                    bet.getNextOpponentBetStatusList().equals(List.of(ProtoBet.UserBetStatus.WAGERRECIEVED))) {
                bet.getNextOpponentBetStatusList().clear();
            }
        }
    }
}
