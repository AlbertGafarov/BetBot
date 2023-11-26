package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.ProtoBet.*;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.gafarov.betservice.telegram.bot.components.Buttons.addCloseButton;
import static ru.gafarov.betservice.telegram.bot.components.Buttons.closeButton;

@Component
@RequiredArgsConstructor
public class FriendsAction implements Action {

    private final BotService botService;
    private final UserService userService;
    private final FriendService friendService;
    private final SubscribeService subscribeService;
    private final PrettyPrinter prettyPrinter;

    @Override
    // /friends/
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        BetSendMessage friendListMessage = new BetSendMessage(chatId);

        List<User> friends = userService.getSubscribes(user);
        if (friends.isEmpty()) {
            friendListMessage.setText("У Вас пока нет друзей");
            friendListMessage.setReplyMarkup(closeButton());
            friendListMessage.setDelTime(30_000);

            botService.sendAndSave(friendListMessage, user, BotMessageType.YOU_HAVE_NOT_FRIENDS, true);
        } else {
            List<List<InlineKeyboardButton>> buttons = friends.stream().map(a -> {
                InlineKeyboardButton nameButton = new InlineKeyboardButton(a.getUsername() + " " + a.getCode());
                nameButton.setCallbackData("/friends/show/" + a.getId());
                InlineKeyboardButton deleteButton = new InlineKeyboardButton("🗑");
                deleteButton.setCallbackData("/friends/delete/" + a.getId());
                return List.of(nameButton, deleteButton);
            }).collect(Collectors.toList());
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            markupInline.setKeyboard(buttons);
            addCloseButton(markupInline);
            friendListMessage.setReplyMarkup(markupInline);
            friendListMessage.setText("Ваши друзья:");

            botService.sendAndSave(friendListMessage, user, BotMessageType.FRIEND_LIST, true);
            botService.delete(update);
        }
    }

    @Override
    public void callback(Update update) {
        String[] command = update.getCallbackQuery().getData().split("/");
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);

        // /friends/show/{id}
        if ("show".equals(command[2])) {
            User friend = userService.getUserById(Long.parseLong(command[3]));
            Friend.FriendInfo friendInfo = friendService.getFriendInfo(user, Long.parseLong(command[3]));
            BetSendMessage sendMessage = new BetSendMessage(chatId);
            sendMessage.setText(prettyPrinter.printFriendInfo(friendInfo));

            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            InlineKeyboardButton showActiveBetsButton = new InlineKeyboardButton("Активные споры " + friendInfo.getActiveBetCount());
            showActiveBetsButton.setCallbackData("/bets/" + BetStatus.ACTIVE + "/with/" + friend.getId());

            InlineKeyboardButton showOfferedBetsButton = new InlineKeyboardButton("Предложенные споры");
            showOfferedBetsButton.setCallbackData("/bets/" + BetStatus.OFFER + "/with/" + friend.getId());

            InlineKeyboardButton disagreementBetsButton = new InlineKeyboardButton("Споры с неочевидным результатом");
            disagreementBetsButton.setCallbackData("/bets/" + BetStatus.DISAGREEMENT + "/with/" + friend.getId());

            InlineKeyboardButton showWaitWagerPayBetsButton = new InlineKeyboardButton("Споры в ожидании вознаграждения");
            showWaitWagerPayBetsButton.setCallbackData("/bets/" + BetStatus.WAIT_WAGER_PAY + "/with/" + friend.getId());

            InlineKeyboardButton showClosedBetsButton = new InlineKeyboardButton("Закрытые споры " + friendInfo.getClosedBetCount());
            showClosedBetsButton.setCallbackData("/bets/" + BetStatus.CLOSED + "/with/" + friend.getId());

            InlineKeyboardButton createBetButton = new InlineKeyboardButton("Создать спор");
            createBetButton.setCallbackData("/create/bet/with/" + friend.getId());

            InlineKeyboardButton deleteButton = new InlineKeyboardButton("🗑");
            deleteButton.setCallbackData("/friends/delete/" + friend.getId());
            InlineKeyboardButton closeButton = new InlineKeyboardButton("✖");
            closeButton.setCallbackData("/close");
            if (userService.findFriendById(user, friend.getId()) != null) {
                buttons.add(List.of(createBetButton));
            }
            buttons.add(List.of(showOfferedBetsButton));
            buttons.add(List.of(showActiveBetsButton));
            buttons.add(List.of(disagreementBetsButton));
            buttons.add(List.of(showWaitWagerPayBetsButton));
            buttons.add(List.of(showClosedBetsButton));
            buttons.add(List.of(deleteButton));
            buttons.add(List.of(closeButton));
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            markupInline.setKeyboard(buttons);
            sendMessage.setReplyMarkup(markupInline);

            botService.sendAndSaveFriend(sendMessage, user, BotMessageType.FRIEND_INFO, friend);

            // /friends/delete/{id}
        } else if ("delete".equals(command[2])) {
            User friend = userService.getUserById(Long.parseLong(command[3]));
            Status status = subscribeService.delete(user, friend);
            if (status.equals(Status.SUCCESS)) {
                BetSendMessage sendMessage = new BetSendMessage(chatId, 10_000);
                sendMessage.setText(friend.getUsername() + " удален(а) из вашего списка");
                botService.sendAndSaveFriend(sendMessage, user, BotMessageType.DELETED_FRIEND, friend);
            }
        }
        botService.delete(update);
    }
}
