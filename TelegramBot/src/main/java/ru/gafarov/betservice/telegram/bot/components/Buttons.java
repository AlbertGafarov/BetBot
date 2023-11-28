package ru.gafarov.betservice.telegram.bot.components;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Buttons {

    public static InlineKeyboardMarkup codeButtons() {
        InlineKeyboardButton codeButton = new InlineKeyboardButton("Code");
        codeButton.setCallbackData("/code");
        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");

        List<InlineKeyboardButton> rowInline = List.of(codeButton, closeButton);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup approveDraftBetButtons(long id) {
        InlineKeyboardButton OK_BUTTON = new InlineKeyboardButton("Ok");
        InlineKeyboardButton CANCEL_BUTTON = new InlineKeyboardButton("Cancel");
        OK_BUTTON.setCallbackData("/draftBet/" + id + "/approve/ok");
        CANCEL_BUTTON.setCallbackData("/draftBet/" + id + "/approve/cancel/");

        List<InlineKeyboardButton> rowInline = List.of(OK_BUTTON, CANCEL_BUTTON);
        List<InlineKeyboardButton> secondRowInline = new ArrayList<>() {{
            InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
            closeButton.setCallbackData("/close");
            add(closeButton);
        }};
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline,secondRowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup nextStatusesButtons(List<UserBetStatus> nextStatuses, long id) {
        List<InlineKeyboardButton> rowInline = nextStatuses.stream().map(a -> {
            InlineKeyboardButton button = new InlineKeyboardButton(a.name());
            button.setCallbackData("/newStatus/" + a.name() + "/" + id);
            return button;
        }).collect(Collectors.toList());
        List<InlineKeyboardButton> secondRowInline = new ArrayList<>() {{
            InlineKeyboardButton pauseButton = new InlineKeyboardButton("⬇");
            pauseButton.setCallbackData("/showBet/" + id);
            InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
            closeButton.setCallbackData("/close");
            add(pauseButton);
            add(closeButton);
        }};
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline, secondRowInline);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup wantChoseFromFriends(DraftBet draftBet){
        List<InlineKeyboardButton> rowInline = new ArrayList<>() {{
            InlineKeyboardButton showMyFriendsButton = new InlineKeyboardButton("Выбрать из списка моих друзей");
            showMyFriendsButton.setCallbackData("/draftBet/" + draftBet.getId() + "/showMyFriends/");
            add(showMyFriendsButton);
        }};
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }
    public static InlineKeyboardMarkup closeButton(){
        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(List.of(closeButton));
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);
        return markupInline;
    }
    public static void addCloseButton(InlineKeyboardMarkup markupInline){

        List<List<InlineKeyboardButton>> buttons = markupInline.getKeyboard();
        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");
        buttons.add(List.of(closeButton));
    }
    public static InlineKeyboardMarkup oneButton(String name, String command){
        InlineKeyboardButton button = new InlineKeyboardButton(name);
        button.setCallbackData(command);
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(List.of(button));
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);
        return markupInline;
    }
}