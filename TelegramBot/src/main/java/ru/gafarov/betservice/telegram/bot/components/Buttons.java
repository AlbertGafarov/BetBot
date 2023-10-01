package ru.gafarov.betservice.telegram.bot.components;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class Buttons {
    private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
    private static final InlineKeyboardButton CODE_BUTTON = new InlineKeyboardButton("Code");
    private static final InlineKeyboardButton CREATE_BUTTON = new InlineKeyboardButton("Create");
    private static final InlineKeyboardButton OK_BUTTON = new InlineKeyboardButton("Ok");
    private static final InlineKeyboardButton CANCEL_BUTTON = new InlineKeyboardButton("Cancel");

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

    public static InlineKeyboardMarkup okAndCancelButtons() {
        OK_BUTTON.setCallbackData("/ok");
        CANCEL_BUTTON.setCallbackData("/cancel");

        List<InlineKeyboardButton> rowInline = List.of(OK_BUTTON, CANCEL_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup approveAndDeclineButtons(long betId) {
        InlineKeyboardButton APPROVE_BUTTON = new InlineKeyboardButton("Approve bet");
        InlineKeyboardButton DECLINE_BUTTON = new InlineKeyboardButton("Decline bet");
        APPROVE_BUTTON.setCallbackData("/approveBet " + betId);
        DECLINE_BUTTON.setCallbackData("/declineBet" + betId);

        List<InlineKeyboardButton> rowInline = List.of(APPROVE_BUTTON, DECLINE_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }
}