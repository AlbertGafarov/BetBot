package ru.gafarov.betservice.converter;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.Bet;
import ru.gafarov.betservice.model.DraftBet;
import ru.gafarov.betservice.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class Converter {

    public Proto.DraftBet toProtoDraftBet(DraftBet draftBet) {
        Proto.DraftBet.Builder builder = Proto.DraftBet.newBuilder()
                .setId(draftBet.getId());
        if (draftBet.getOpponentName() != null) {
            builder.setOpponentName(draftBet.getOpponentName());
        }
        if (draftBet.getWager() != null) {
            builder.setWager(draftBet.getWager());
        }
        if (draftBet.getDefinition() != null) {
            builder.setDefinition(draftBet.getDefinition());
        }
        if (draftBet.getOpponentCode() != 0) {
            builder.setOpponentCode(draftBet.getOpponentCode());
        }
        if (draftBet.getFinishDate() != null) {
            builder.setFinishDate(toTimestamp(draftBet.getFinishDate()));
        }

        return builder.build();
    }
    public Proto.Bet toProtoBet(Bet bet) {
        Proto.Bet.Builder builder = Proto.Bet.newBuilder()
                .setId(bet.getId())
                .setOpponent(userToProto(bet.getOpponent()))
                .setInitiator(userToProto(bet.getInitiator()))
                .setWager(bet.getWager())
                .setDefinition(bet.getDefinition())
                .setFinishDate(toTimestamp(bet.getFinishDate()))
                .setOpponentStatus(bet.getOpponentBetStatus())
                .setInitiatorStatus(bet.getInitiatorBetStatus());
        return builder.build();
    }

    public Proto.User userToProto(User user) {
        if (user == null) return null;
        Proto.User.Builder builder = Proto.User.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setCode(user.getCode())
                .setChatId(user.getChatId())
                .setChatStatus(user.getChatStatus());
        if (user.getDraftBet() != null) {
            builder.setDraftBet(toProtoDraftBet(user.getDraftBet()));
        }

        return builder.build();

    }

    private User protoUserToUser(Proto.User protoUser) {
        User user = new User();
        user.setId(protoUser.getId());
        user.setChatId(protoUser.getChatId());
        user.setUsername(protoUser.getUsername());
        user.setChatStatus(protoUser.getChatStatus());
        user.setCode(protoUser.getCode());

        return user;
    }

    private Timestamp toTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
    public LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
