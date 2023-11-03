package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.Proto.BotMessage;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendsAction implements Action {

    private final BotService botService;
    private final UserService userService;
    private final BotMessageService botMessageService;

    @Override
    // /friends/
    public List<BetSendMessage> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);

        List<User> friends = userService.getFriends(user);

        BetSendMessage friendListMessage = new BetSendMessage(chatId);

        List<List<InlineKeyboardButton>> buttons = friends.stream().map(a -> {
            InlineKeyboardButton nameButton = new InlineKeyboardButton(a.getUsername() + " " + a.getCode());
            nameButton.setCallbackData("/nothing");
            InlineKeyboardButton deleteButton = new InlineKeyboardButton("🗑");
            deleteButton.setCallbackData("/friends/delete/" + a.getId());
            return List.of(nameButton, deleteButton);
        }).collect(Collectors.toList());
        InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
        closeButton.setCallbackData("/close");
        buttons.add(List.of(closeButton));
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(buttons);
        friendListMessage.setReplyMarkup(markupInline);
        friendListMessage.setText("Ваши друзья:");

        botMessageService.save(BotMessage.newBuilder().setTgMessageId(botService.send(friendListMessage))
                .setType(BotMessageType.FRIEND_LIST).setUser(user).build());
        botService.delete(update);
        return new ArrayList<>();
    }

    @Override
    public List<BetSendMessage> callback(Update update) {
        return null;
    }
}
