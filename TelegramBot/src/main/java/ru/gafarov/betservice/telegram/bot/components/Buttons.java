package ru.gafarov.betservice.telegram.bot.components;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.Proto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Buttons {
    private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
    private static final InlineKeyboardButton CODE_BUTTON = new InlineKeyboardButton("Code");
    private static final InlineKeyboardButton CREATE_BUTTON = new InlineKeyboardButton("Create");

    public static InlineKeyboardMarkup inlineMarkup() {
        START_BUTTON.setCallbackData("/start");
        CODE_BUTTON.setCallbackData("/code");
        CREATE_BUTTON.setCallbackData("/create");

        List<InlineKeyboardButton> rowInline = List.of(CODE_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup approveDraftBetButtons() {
        InlineKeyboardButton OK_BUTTON = new InlineKeyboardButton("Ok");
        InlineKeyboardButton CANCEL_BUTTON = new InlineKeyboardButton("Cancel");
        OK_BUTTON.setCallbackData("/draftBet/ok");
        CANCEL_BUTTON.setCallbackData("/draftBet/cancel");

        List<InlineKeyboardButton> rowInline = List.of(OK_BUTTON, CANCEL_BUTTON);
        List<InlineKeyboardButton> secondRowInline = new ArrayList<>() {{
            InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
            closeButton.setCallbackData("/closeBet");
            add(closeButton);
        }};
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline,secondRowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup nextStatusesButtons(List<Proto.BetStatus> nextStatuses, long id) {
        List<InlineKeyboardButton> rowInline = nextStatuses.stream().map(a -> {
            InlineKeyboardButton button = new InlineKeyboardButton(a.name());
            button.setCallbackData("/newStatus/" + a.name() + "/" + id);
            return button;
        }).collect(Collectors.toList());
        List<InlineKeyboardButton> secondRowInline = new ArrayList<>() {{
            InlineKeyboardButton pauseButton = new InlineKeyboardButton("⬇");
            pauseButton.setCallbackData("/showBet/" + id);
            InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
            closeButton.setCallbackData("/closeBet");
            add(pauseButton);
            add(closeButton);
        }};
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline, secondRowInline);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }
}