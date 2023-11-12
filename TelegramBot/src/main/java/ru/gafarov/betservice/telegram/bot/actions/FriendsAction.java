package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.Proto.BotMessage;
import ru.gafarov.bet.grpcInterface.Proto.BotMessageType;
import ru.gafarov.bet.grpcInterface.Proto.Status;
import ru.gafarov.bet.grpcInterface.Proto.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.SubscribeService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.gafarov.betservice.telegram.bot.components.Buttons.addCloseButton;
import static ru.gafarov.betservice.telegram.bot.components.Buttons.closeButton;

@Component
@RequiredArgsConstructor
public class FriendsAction implements Action {

    private final BotService botService;
    private final UserService userService;
    private final BotMessageService botMessageService;
    private final SubscribeService subscribeService;

    @Override
    // /friends/
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        BetSendMessage friendListMessage = new BetSendMessage(chatId);

        List<User> friends = userService.getFriends(user);
        if (friends.isEmpty()) {
            friendListMessage.setText("У Вас пока нет друзей");
            friendListMessage.setReplyMarkup(closeButton());
            friendListMessage.setDelTime(30_000);

            botMessageService.save(BotMessage.newBuilder().setTgMessageId(botService.sendTimeIsUpMessage(friendListMessage))
                    .setType(BotMessageType.YOU_HAVE_NOT_FRIENDS).setUser(user).build());
        } else {
            List<List<InlineKeyboardButton>> buttons = friends.stream().map(a -> {
                InlineKeyboardButton nameButton = new InlineKeyboardButton(a.getUsername() + " " + a.getCode());
                nameButton.setCallbackData("/friends/show/{id}");
                InlineKeyboardButton deleteButton = new InlineKeyboardButton("🗑");
                deleteButton.setCallbackData("/friends/delete/" + a.getId());
                return List.of(nameButton, deleteButton);
            }).collect(Collectors.toList());
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            markupInline.setKeyboard(buttons);
            addCloseButton(markupInline);
            friendListMessage.setReplyMarkup(markupInline);
            friendListMessage.setText("Ваши друзья:");

            botMessageService.save(BotMessage.newBuilder().setTgMessageId(botService.sendTimeIsUpMessage(friendListMessage))
                    .setType(BotMessageType.FRIEND_LIST).setUser(user).build());
            botService.delete(update);
        }
    }

    @Override
    public void callback(Update update) {
        String[] command = update.getCallbackQuery().getData().split("/");
        // /friends/delete/{id}
        if ("delete".equals(command[2])) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            User user = userService.getUser(chatId);
            User friend = userService.getUserById(Long.parseLong(command[3]));
            Status status = subscribeService.delete(user, friend);
            if (status.equals(Status.SUCCESS)) {
                BetSendMessage sendMessage = new BetSendMessage(chatId, 10_000);
                sendMessage.setText(friend.getUsername() + " удален(а) из вашего списка");
                botService.sendAndSave(sendMessage, user, BotMessageType.DELETED_FRIEND);
            }
        }
        botService.delete(update);
    }
}
