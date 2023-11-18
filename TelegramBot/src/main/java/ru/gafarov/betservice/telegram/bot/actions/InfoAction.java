package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.Info.InfoType;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.InfoService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InfoAction implements Action {

    private final InfoService infoService;
    private final BotService botService;
    private final UserService userService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);

        // /info
        BetSendMessage sendMessage = infoService.getInfo(InfoType.MENU, chatId);

        InlineKeyboardButton aboutBotButton = new InlineKeyboardButton("О боте");
        aboutBotButton.setCallbackData("/info/aboutBot");

        InlineKeyboardButton howAddFriendButton = new InlineKeyboardButton("Как добавить друга");
        howAddFriendButton.setCallbackData("/info/howAddFriend");

        InlineKeyboardButton howCreateBetButton = new InlineKeyboardButton("Как создать спор");
        howCreateBetButton.setCallbackData("/info/howCreateBet");

        InlineKeyboardButton aboutPersonalDataButton = new InlineKeyboardButton("О персональных данных");
        aboutPersonalDataButton.setCallbackData("/info/aboutPersonalData");

        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(List.of(aboutBotButton));
        buttons.add(List.of(howAddFriendButton));
        buttons.add(List.of(howCreateBetButton));
        buttons.add(List.of(aboutPersonalDataButton));
        buttons.add(List.of(closeButton));

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupInline);
        botService.delete(update);
        botService.sendAndSave(sendMessage, user, BotMessageType.INFO, true);
    }

    @Override
    public void callback(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        Proto.User user = userService.getUser(chatId);
        String[] command = update.getCallbackQuery().getData().split("/");

        // /info/aboutBot
        if("aboutBot".equals(command[2])){
            BetSendMessage sendMessage = infoService.getInfo(InfoType.ABOUT_BOT, chatId);
            sendMessage.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(sendMessage, user, BotMessageType.INFO_ABOUT_BOT, true);

            // /info/howAddFriend
        } else if ("howAddFriend".equals(command[2])){
            BetSendMessage sendMessage = infoService.getInfo(InfoType.HOW_ADD_FRIEND, chatId);
            sendMessage.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(sendMessage, user, BotMessageType.INFO_HOW_ADD_FRIEND, true);

            // /info/howCreateBet
        } else if ("howCreateBet".equals(command[2])){
            BetSendMessage sendMessage = infoService.getInfo(InfoType.HOW_CREATE_BET, chatId);
            sendMessage.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(sendMessage, user, BotMessageType.INFO_HOW_CREATE_BET, true);

            // /info/howCreateBet
        } else if ("aboutPersonalData".equals(command[2])){
            BetSendMessage sendMessage = infoService.getInfo(InfoType.ABOUT_PERSONAL_DATA, chatId);
            sendMessage.setReplyMarkup(Buttons.closeButton());
            botService.sendAndSave(sendMessage, user, BotMessageType.INFO_ABOUT_PERSONAL_DATA, true);
        }
    }
}
