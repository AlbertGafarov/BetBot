package ru.gafarov.betservice.converter;

import lombok.experimental.UtilityClass;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.entity.Bet;

@UtilityClass
public class BetConverter {
    public Bet toBet(ProtoBet.Bet protoBet) {
        Bet bet = new Bet();
        if (protoBet.getId() > 0) {
            bet.setId(protoBet.getId());
        }
        return bet;
    }

    public ProtoBet.Bet toProtoBet(Bet bet) {
        ProtoBet.Bet.Builder builder = ProtoBet.Bet.newBuilder()
                .setId(bet.getId())
                .setOpponent(UserConverter.toProtoUser(bet.getOpponent()))
                .setInitiator(UserConverter.toProtoUser(bet.getInitiator()))
                .setDefinition(bet.getDefinition())
                .setFinishDate(DateTimeConverter.toTimestamp(bet.getFinishDate()))
                .setOpponentStatus(bet.getOpponentBetStatus())
                .setInitiatorStatus(bet.getInitiatorBetStatus())
                .setInverseDefinition(bet.isInverseDefinition())
                .addAllInitiatorNextStatuses(bet.getNextInitiatorBetStatusList())
                .addAllOpponentNextStatuses(bet.getNextOpponentBetStatusList());
        if (bet.getWager() != null) {
            builder.setWager(bet.getWager());
        }
        if (bet.getArguments() != null) {
            builder.addAllArguments(ArgumentConverter.toProtoArguments(bet.getArguments()));
        }
        return builder.build();
    }

}