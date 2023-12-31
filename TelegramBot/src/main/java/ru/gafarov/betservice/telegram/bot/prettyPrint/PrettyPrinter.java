package ru.gafarov.betservice.telegram.bot.prettyPrint;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.ProtoBet.Bet;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class PrettyPrinter {

    public String printDraftBet(DraftBet draftBet) {
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

    public String printDraftBetFromForwardMessage(DraftBet draftBet) {
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

    public String printOfferBet(Bet bet) {
        if (bet.getInverseDefinition()) {
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

    public String printBet(Bet bet) {
        User author = bet.getInverseDefinition() ? bet.getOpponent() : bet.getInitiator();
        User rival = bet.getInverseDefinition() ? bet.getInitiator() : bet.getOpponent();

        return "<b>Спор</b>\n" +
                author.getUsername() + " " + author.getCode() +
                "\nСчитает что: " + bet.getDefinition() +
                "\nОспаривает: " + rival.getUsername() + " " + rival.getCode() +
                "\nДата окончания спора: " + fromGoogleTimestampToStr(bet.getFinishDate()) +
                (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                "\nСтатус " + bet.getInitiator().getUsername() + ": " + bet.getInitiatorStatus() +
                "\nСтатус " + bet.getOpponent().getUsername() + ": " + bet.getOpponentStatus();
    }

    public String printFriendInfo(Friend.FriendInfo friendInfo) {
        return "<b>" + friendInfo.getUser().getUsername() + " " + friendInfo.getUser().getCode() + "</b>" +
                (friendInfo.getSubscribed() ? "\nПодписан(а) на Вас" : "\nНе подписан(а) на Вас") +
                (friendInfo.getSubscribed() ? "\nПобед во всех спорах: " + (double) Math.round(friendInfo.getTotalWinPercent() * 100) / 100 + " %" : "") +
                (friendInfo.getSubscribed() ? "\nНичьих во всех спорах: " + (double) Math.round(friendInfo.getTotalStandoffPercent() * 100) / 100 + " %" : "") +
                "\nНаших завершенных споров: " + friendInfo.getClosedBetCount() +
                "\nПобед в наших спорах: " + (double) Math.round(friendInfo.getWinPercent() * 100) / 100 + " %" +
                "\nНичьих в наших спорах: " + (double) Math.round(friendInfo.getStandoffPercent() * 100) / 100 + " %" +
                "\nНаших активных споров: " + friendInfo.getActiveBetCount();
    }
}