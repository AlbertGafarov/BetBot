package ru.gafarov.betservice.telegram.bot.prettyPrint;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.Proto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class PrettyPrinter {

    public String printDraftBet(Proto.DraftBet draftBet) {
        return "Оппонент: " + draftBet.getOpponentName() +
                " " + draftBet.getOpponentCode() +
                "\nСуть спора: " + draftBet.getDefinition() +
                "\nВознаграждение победителю: " + draftBet.getWager() +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(draftBet.getFinishDate());
    }

    private String fromGoogleTimestampToStr(final Timestamp googleTimestamp) {
        LocalDateTime localDateTime = Instant.ofEpochSecond(googleTimestamp.getSeconds(), googleTimestamp.getNanos())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return localDateTime.format(formatter);
    }

    public LocalDateTime fromGoogleTimestampUTC(final Timestamp googleTimestamp) {
        return Instant.ofEpochSecond(googleTimestamp.getSeconds(), googleTimestamp.getNanos())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String printOfferBet(Proto.Bet bet) {
        return  "<b>Спор</b>\n" + bet.getInitiator().getUsername() + " " + bet.getInitiator().getCode() +
                "\nсчитает что:\n" + bet.getDefinition() +
                "\nи предлагает Вам оспорить это утверждение." +
                "\nВознаграждение победителю спора: " + bet.getWager() +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                "\nГотовы оспорить?";
    }

    public String printBet(Proto.Bet bet) {
        return "<b>Спор</b>\n" +
                bet.getInitiator().getUsername() +
                " " + bet.getInitiator().getCode() +
                "\nСчитает что: " + bet.getDefinition() +
                "\nОспаривает: " + bet.getOpponent().getUsername() +
                " " + bet.getOpponent().getCode() +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                "\nВознаграждение победителю: " + bet.getWager() +
                "\nСтатус " + bet.getInitiator().getUsername() + ": " + bet.getInitiatorStatus() +
                "\nСтатус " + bet.getOpponent().getUsername() + ": " + bet.getOpponentStatus();
    }
}