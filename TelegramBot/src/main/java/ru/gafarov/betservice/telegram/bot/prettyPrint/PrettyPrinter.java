package ru.gafarov.betservice.telegram.bot.prettyPrint;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Proto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class PrettyPrinter {

    public String printDraftBet(Proto.DraftBet draftBet) {
        return "Оппонент: " + draftBet.getOpponentName() +
                "\nКод оппонента: " + draftBet.getOpponentCode() +
                "\nСуть спора: " + draftBet.getDefinition() +
                "\nВознаграждение победителю:" + draftBet.getWager() +
                "\nДата окончания спора: " + fromGoogleTimestampUTC(draftBet.getFinishDate());
    }

    private String fromGoogleTimestampUTC(final Timestamp googleTimestamp) {
        LocalDateTime localDateTime = Instant.ofEpochSecond(googleTimestamp.getSeconds(), googleTimestamp.getNanos())
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return localDateTime.format(formatter);
    }

    public String printOfferBet(Proto.Bet bet) {
        return  bet.getInitiator().getUsername() +
                "\ncode:" + bet.getInitiator().getCode() +
                "\nсчитает что:\n" + bet.getDefinition() +
                "\nи предлагет Вам оспорить это утверждение." +
                "\nВознаграждение победителю спора:" + bet.getWager() +
                "\nДата окончания спора: " + fromGoogleTimestampUTC(bet.getFinishDate()) +
                "\nГотовы оспорить?";
    }

    public String printBet(Proto.Bet bet) {
        return "Инициатор: " + bet.getInitiator().getUsername() +
                "\nКод инициатора: " + bet.getOpponent().getCode() +
                "\nОппонент: " + bet.getOpponent().getUsername() +
                "\nКод оппонента: " + bet.getOpponent().getCode() +
                "\nСуть спора: " + bet.getDefinition() +
                "\nВознаграждение победителю:" + bet.getWager() +
                "\nДата окончания спора: " + fromGoogleTimestampUTC(bet.getFinishDate()) +
                "\nСтатус инициатора:" + bet.getInitiatorStatus() +
                "\nСтатус оппонента:" + bet.getOpponentStatus();
    }
}
