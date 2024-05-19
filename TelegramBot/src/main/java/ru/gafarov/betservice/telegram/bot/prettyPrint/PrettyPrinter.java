package ru.gafarov.betservice.telegram.bot.prettyPrint;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.Bet;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Component
public class PrettyPrinter {

    private static final String FINISH_DATE = "\n\uD83D\uDCC5 Дата окончания спора: ";
    private static final String BET = "\uD83E\uDD1D <b>Спор</b>\n";

    public String printDraftBet(DraftBet draftBet) {
        if (draftBet.getInverseDefinition()) {
            return "Оппонент: " + draftBet.getOpponentName() +
                    " " + draftBet.getOpponentCode() +
                    "\nНаписал(а): " + draftBet.getDefinition() +
                    "\nВы оспариваете это утверждение" +
                    (draftBet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + draftBet.getWager()) +
                    FINISH_DATE + fromGoogleTimestampToStr(draftBet.getFinishDate());
        }
        return "Оппонент: " + draftBet.getOpponentName() +
                " " + draftBet.getOpponentCode() +
                "\nСуть спора: " + draftBet.getDefinition() +
                (draftBet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + draftBet.getWager()) +
                FINISH_DATE + fromGoogleTimestampToStr(draftBet.getFinishDate());
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
            return BET +
                    "Вы написали: " + bet.getDefinition() +
                    "\n" + bet.getInitiator().getUsername() + " " + bet.getInitiator().getCode() + " ставит это под сомнение и готов(а) оспорить." +
                    (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                    FINISH_DATE + fromGoogleTimestampToStr(bet.getFinishDate()) +
                    "\nВы готовы в этом поучаствовать?";
        }
        return BET + bet.getInitiator().getUsername() + " " + bet.getInitiator().getCode() +
                "\nсчитает что:\n" + bet.getDefinition() +
                "\nи предлагает Вам оспорить это утверждение." +
                (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                FINISH_DATE + fromGoogleTimestampToStr(bet.getFinishDate()) +
                "\nГотовы оспорить?";
    }

    public String printBet(Bet bet) {
        User author = bet.getInverseDefinition() ? bet.getOpponent() : bet.getInitiator();
        User rival = bet.getInverseDefinition() ? bet.getInitiator() : bet.getOpponent();
        StringBuilder arguments = new StringBuilder();
        if (bet.getArgumentsCount() > 0) {
            List<ProtoBet.Argument> argumentList = new LinkedList<>(bet.getArgumentsList());
            argumentList.sort(Comparator.comparingLong(a -> a.getTimestamp().getSeconds()));
            arguments.append("\n<b>Аргументы</b>");
            for (ProtoBet.Argument argument : argumentList) {
                arguments.append("\n").append(String.format("%s <b>%s</b>: %s"
                        , fromGoogleTimestampToStr(argument.getTimestamp()), argument.getAuthor().getUsername(), argument.getText()));
            }
        }
        return BET +
                author.getUsername() + " " + author.getCode() +
                "\nСчитает что: " + bet.getDefinition() +
                "\nОспаривает: " + rival.getUsername() + " " + rival.getCode() +
                FINISH_DATE + fromGoogleTimestampToStr(bet.getFinishDate()) +
                (bet.getWager().isEmpty() ? "" : "\nВознаграждение победителю: " + bet.getWager()) +
                "\nСтатус " + bet.getInitiator().getUsername() + ": " + bet.getInitiatorStatus() +
                "\nСтатус " + bet.getOpponent().getUsername() + ": " + bet.getOpponentStatus() +
                arguments;
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