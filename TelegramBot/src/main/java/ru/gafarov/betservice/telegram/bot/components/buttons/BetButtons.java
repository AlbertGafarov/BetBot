package ru.gafarov.betservice.telegram.bot.components.buttons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.ProtoBet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BetButtons {

    public static InlineKeyboardMarkup getBetButtons(ProtoBet.Bet bet, boolean forInitiator) {
        List<InlineKeyboardButton> rowInline = getNextStatusesButtons(forInitiator ? bet.getInitiatorNextStatusesList()
                : bet.getOpponentNextStatusesList(), bet.getId());
        List<InlineKeyboardButton> secondRowInline = getSecondRowInline(bet, forInitiator);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline, secondRowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    private static List<InlineKeyboardButton> getNextStatusesButtons(List<ProtoBet.UserBetStatus> nextStatuses, long id) {
        return nextStatuses.stream().map(a -> {
            InlineKeyboardButton button = new InlineKeyboardButton(a.name());
            button.setCallbackData("/newStatus/" + a.name() + "/" + id);
            return button;
        }).collect(Collectors.toList());
    }

    public static List<InlineKeyboardButton> getSecondRowInline(ProtoBet.Bet bet, boolean forInitiator) {
        List<InlineKeyboardButton> secondRowInline = new ArrayList<>();
        InlineKeyboardButton pauseButton = new InlineKeyboardButton("⬇");
        pauseButton.setCallbackData("/showBet/" + bet.getId());
        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");
        secondRowInline.add(pauseButton);

        List<ProtoBet.Argument> arguments = new LinkedList<>(bet.getArgumentsList());
        arguments.sort((a1, a2) -> Long.compare(a2.getTimestamp().getSeconds(), a1.getTimestamp().getSeconds()));
        Optional<ProtoBet.Argument> optionalArgument = arguments.stream().findFirst();
        if (optionalArgument.isEmpty() && !forInitiator ||
                optionalArgument.isPresent() &&
                        (forInitiator && optionalArgument.get().getAuthor().equals(bet.getOpponent()) ||
                                !forInitiator && optionalArgument.get().getAuthor().equals(bet.getInitiator())) &&
                        (bet.getBetStatus().equals(ProtoBet.BetStatus.ACTIVE) ||
                                bet.getBetStatus().equals(ProtoBet.BetStatus.WAIT_WAGER_PAY) ||
                                bet.getBetStatus().equals(ProtoBet.BetStatus.OFFER))
        ) {
            InlineKeyboardButton writeArgumentButton = new InlineKeyboardButton("\uD83D\uDCAC");
            writeArgumentButton.setCallbackData("/argument/write/" + bet.getId());
            secondRowInline.add(writeArgumentButton);
        }
        secondRowInline.add(closeButton);
        return secondRowInline;
    }
}
