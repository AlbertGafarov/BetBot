package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.Info.InfoType;
import ru.gafarov.bet.grpcInterface.SecretKey;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.*;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecretAction implements Action {

    private final InfoService infoService;
    private final BotService botService;
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final SecretKeyService secretKeyService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);

        // /secret
        showSecretMenu(update, chatId, user);
    }

    private void showSecretMenu(Update update, long chatId, User user) {
        BetSendMessage sendMessage = infoService.getInfo(InfoType.MENU_SECRET, chatId);
        SecretKey.MessageWithKey messageWithKey = secretKeyService.getSecretMessage(user);
        InlineKeyboardButton setSecretKeyButton = new InlineKeyboardButton(
                (messageWithKey != null ? "Изменить" : "Установить") + " ключ шифрования");
        setSecretKeyButton.setCallbackData("/secret/key/set");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (user.getEncryptionEnabled()) {
            buttons.add(List.of(setSecretKeyButton));

            if (messageWithKey != null) {
                InlineKeyboardButton showSecretKeyButton = new InlineKeyboardButton("Показать ключ шифрования");
                showSecretKeyButton.setCallbackData("/secret/key/show/" + messageWithKey.getSecretKey());
                buttons.add(List.of(showSecretKeyButton));
            }
            InlineKeyboardButton disableEncryptionButton = new InlineKeyboardButton("Выключить шифрование");
            disableEncryptionButton.setCallbackData("/secret/encryption/disable");
            buttons.add(List.of(disableEncryptionButton));
        } else {
            InlineKeyboardButton enableEncryptionButton = new InlineKeyboardButton("Включить шифрование");
            enableEncryptionButton.setCallbackData("/secret/encryption/enable");
            buttons.add(List.of(enableEncryptionButton));
        }

        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");

        buttons.add(List.of(closeButton));

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupInline);
        botService.delete(update);
        botService.sendAndSave(sendMessage, user, BotMessageType.SECRET_MENU, true);
    }

    @Override
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);
        String[] command = update.getCallbackQuery().getData().split("/");

        // /sercet/setKey
        switch (command[2]) {
            case "key":
                switch (command[3]) {
                    case "set":
                        if (user == null) {
                            user = authorizationService.addNewUser(update, chatId);
                        }
                        userService.setChatStatus(user, UserOuterClass.ChatStatus.WAIT_SECRET_KEY);
                        BetSendMessage sendInfoMessage = new BetSendMessage(chatId);
                        sendInfoMessage.setText("Введите секретный ключ");
                        botService.sendAndSave(sendInfoMessage, user, BotMessageType.ENTER_SECRET_KEY, true);
                        botService.delete(update);
                        break;
                    case "show":
                        BetSendMessage message = new BetSendMessage(chatId);
                        message.setText("Ваш ключ шифрования: <tg-spoiler>" + command[4] + "</tg-spoiler>");
                        botService.sendAndSave(message, user, BotMessageType.SECRET_KEY, true);
                        break;
                }
                break;
            case "encryption":
                switch (command[3]) {
                    case "enable":
                        user = userService.setEncryptionStatus(user, true);
                        break;
                    case "disable":
                        user = userService.setEncryptionStatus(user, false);
                        break;
                }
                showSecretMenu(update, chatId, user);
                break;
        }
    }
}
