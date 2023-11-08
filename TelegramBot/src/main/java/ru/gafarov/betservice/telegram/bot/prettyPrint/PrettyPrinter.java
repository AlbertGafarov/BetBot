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
        if (draftBet.getInverseDefinition()) {
            return "Оппонент: " + draftBet.getOpponentName() +
                    " " + draftBet.getOpponentCode() +
                    "\nНаписал(а): " + draftBet.getDefinition() +
                    "\nВы оспариваете это утверждение" +
                    (draftBet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + draftBet.getWager()) +
                    "\nДата окончания спора: " + fromGoogleTimestampToStr(draftBet.getFinishDate());
        }
        return "Оппонент: " + draftBet.getOpponentName() +
                " " + draftBet.getOpponentCode() +
                "\nСуть спора: " + draftBet.getDefinition() +
                (draftBet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + draftBet.getWager()) +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(draftBet.getFinishDate());
    }

    public String printDraftBetFromForwardMessage(Proto.DraftBet draftBet) {
            return "Оппонент: " + draftBet.getOpponentName() +
                    " " + draftBet.getOpponentCode() +
                    "\nНаписал(а): " + draftBet.getDefinition() +
                    "\nВы оспариваете это утверждение" +
                    "\n\nВведите вознаграждение победителю";
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
        if(bet.getInverseDefinition()) {
            return "<b>Спор</b>\n" +
                    "Вы написали: " + bet.getDefinition() +
                    "\n" + bet.getInitiator().getUsername() + " " + bet.getInitiator().getCode() + " ставит это под сомнение и готов(а) оспорить." +
                    (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                    "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                    "\nВы готовы в этом поучаствовать?";
        }
        return "<b>Спор</b>\n" + bet.getInitiator().getUsername() + " " + bet.getInitiator().getCode() +
                "\nсчитает что:\n" + bet.getDefinition() +
                "\nи предлагает Вам оспорить это утверждение." +
                (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                "\nГотовы оспорить?";
    }

    public String printBet(Proto.Bet bet) {
        Proto.User author = bet.getInverseDefinition() ? bet.getOpponent() : bet.getInitiator();
        Proto.User rival  = bet.getInverseDefinition() ? bet.getInitiator() : bet.getOpponent();

        return "<b>Спор</b>\n" +
                author.getUsername() + " " + author.getCode() +
                "\nСчитает что: " + bet.getDefinition() +
                "\nОспаривает: " + rival.getUsername() + " " + rival.getCode() +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                "\nСтатус " + bet.getInitiator().getUsername() + ": " + bet.getInitiatorStatus() +
                "\nСтатус " + bet.getOpponent().getUsername() + ": " + bet.getOpponentStatus();
    }
}