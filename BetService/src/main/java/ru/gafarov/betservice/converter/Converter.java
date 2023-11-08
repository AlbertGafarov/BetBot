package ru.gafarov.betservice.converter;

import com.google.protobuf.Timestamp;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class Converter {

    public Proto.DraftBet toProtoDraftBet(DraftBet draftBet) {
        Proto.DraftBet.Builder builder = Proto.DraftBet.newBuilder()
                .setId(draftBet.getId())
                .setInverseDefinition(draftBet.isInverseDefinition());
        if (draftBet.getInitiator() != null) {
            builder.setInitiator(toProtoUser(draftBet.getInitiator()));
        }
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
                .setOpponent(toProtoUser(bet.getOpponent()))
                .setInitiator(toProtoUser(bet.getInitiator()))
                .setDefinition(bet.getDefinition())
                .setFinishDate(toTimestamp(bet.getFinishDate()))
                .setOpponentStatus(bet.getOpponentBetStatus())
                .setInitiatorStatus(bet.getInitiatorBetStatus())
                .setInverseDefinition(bet.isInverseDefinition())
                .addAllInitiatorNextStatuses(bet.getNextInitiatorBetStatusList())
                .addAllOpponentNextStatuses(bet.getNextOpponentBetStatusList());
        if (bet.getWager() != null) {
            builder.setWager(bet.getWager());
        }
        return builder.build();
    }

    public Proto.User toProtoUser(User user) {
        if (user == null) return null;
        Proto.User.Builder builder = Proto.User.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setCode(user.getCode())
                .setChatId(user.getChatId())
                .setChatStatus(user.getChatStatus());
        return builder.build();
    }

    public Timestamp toTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public User toUser(Proto.User protoUser) {
        User user = new User();
        user.setId(protoUser.getId());
        user.setCode(protoUser.getCode());
        user.setUsername(protoUser.getUsername());
        user.setChatId(protoUser.getChatId());
        return user;
    }

    public BotMessage toBotMessage(Proto.BotMessage protoBotMessage) {
        BotMessage botMessage = new BotMessage();
        if (protoBotMessage.getDraftBet().getId() > 0) {
            botMessage.setDraftBet(toDraftBet(protoBotMessage.getDraftBet()));
        }
        botMessage.setTgMessageId(protoBotMessage.getTgMessageId());
        botMessage.setUser(toUser(protoBotMessage.getUser()));
        botMessage.setMessageType(protoBotMessage.getType());

        return botMessage;
    }

    public DraftBet toDraftBet(Proto.DraftBet protoDraftBet) {
        DraftBet draftBet = new DraftBet();
        draftBet.setId(protoDraftBet.getId());
        draftBet.setInitiator(toUser(protoDraftBet.getInitiator()));
        draftBet.setOpponentCode(protoDraftBet.getOpponentCode());
        draftBet.setOpponentName(protoDraftBet.getOpponentName());
        draftBet.setDefinition(protoDraftBet.getDefinition());
        draftBet.setWager(protoDraftBet.getWager());
        draftBet.setInverseDefinition(protoDraftBet.getInverseDefinition());
        if (protoDraftBet.getFinishDate().getSeconds() != 0) {
            draftBet.setFinishDate(toLocalDateTime(protoDraftBet.getFinishDate()));
        }

        return draftBet;
    }

    public Proto.BotMessage toProtoBotMessage(BotMessage botMessage) {
        val builder = Proto.BotMessage.newBuilder()
                .setId(botMessage.getId())
                .setTgMessageId(botMessage.getTgMessageId())
                .setType(botMessage.getMessageType())
                .setUser(toProtoUser(botMessage.getUser()));
        if (botMessage.getDraftBet() != null) {
            builder.setDraftBet(toProtoDraftBet(botMessage.getDraftBet()));
        }
        if (botMessage.getStatus().equals(Status.DELETED)) {
            builder.setIsDeleted(true);
        }
        return builder.build();
    }
}
