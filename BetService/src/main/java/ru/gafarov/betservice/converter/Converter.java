package ru.gafarov.betservice.converter;

import com.google.protobuf.Timestamp;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.betservice.entity.BotMessage;
import ru.gafarov.betservice.entity.DraftBet;
import ru.gafarov.betservice.model.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class Converter {

    public DrBet.DraftBet toProtoDraftBet(DraftBet draftBet) {
        DrBet.DraftBet.Builder builder = DrBet.DraftBet.newBuilder()
                .setId(draftBet.getId())
                .setInverseDefinition(draftBet.isInverseDefinition());
        if (draftBet.getInitiator() != null) {
            builder.setInitiator(UserConverter.toProtoUser(draftBet.getInitiator()));
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


    public BotMessage toBotMessage(BotMessageOuterClass.BotMessage protoBotMessage) {
        BotMessage botMessage = new BotMessage();
        if (protoBotMessage.getDraftBet().getId() > 0) {
            botMessage.setDraftBet(toDraftBet(protoBotMessage.getDraftBet()));
        }
        if (protoBotMessage.getFriend().getId() > 0) {
            botMessage.setFriend(UserConverter.toUser(protoBotMessage.getFriend()));
        }
        if (protoBotMessage.getBet().getId() > 0) {
            botMessage.setBet(BetConverter.toBet(protoBotMessage.getBet()));
        }
        botMessage.setTgMessageId(protoBotMessage.getTgMessageId());
        botMessage.setUser(UserConverter.toUser(protoBotMessage.getUser()));
        botMessage.setMessageType(protoBotMessage.getType());

        return botMessage;
    }

    public DraftBet toDraftBet(DrBet.DraftBet protoDraftBet) {
        DraftBet draftBet = new DraftBet();
        draftBet.setId(protoDraftBet.getId());
        draftBet.setInitiator(UserConverter.toUser(protoDraftBet.getInitiator()));
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

    public BotMessageOuterClass.BotMessage toProtoBotMessage(BotMessage botMessage) {
        val builder = BotMessageOuterClass.BotMessage.newBuilder()
                .setId(botMessage.getId())
                .setTgMessageId(botMessage.getTgMessageId())
                .setType(botMessage.getMessageType())
                .setUser(UserConverter.toProtoUser(botMessage.getUser()));
        if (botMessage.getDraftBet() != null) {
            builder.setDraftBet(toProtoDraftBet(botMessage.getDraftBet()));
        }
        if (botMessage.getStatus().equals(Status.DELETED)) {
            builder.setIsDeleted(true);
        }
        return builder.build();
    }
}
