package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ResponseUser;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.bet.grpcInterface.UserServiceGrpc;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.Buttons;

import static ru.gafarov.betservice.telegram.bot.components.buttons.Buttons.closeButton;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserServiceGrpc.UserServiceBlockingStub grpcUserStub;
    private final UserService userService;
    private final BotMessageService botMessageService;
    private final BotService botService;

    public void authorization(Update update) {
        long chatId = update.getMessage().getChatId();
        User user;
        ResponseUser responseMessage = grpcUserStub.getUser(User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            user = responseMessage.getUser();
            botMessageService.deleteWithoutDraft(DraftBet.newBuilder().build(), responseMessage.getUser());
        } else {
            user = addNewUser(update, chatId);
        }
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("Привет <b>" + user.getUsername() + "</b>!\nInfo о боте: /info");
        userService.setChatStatus(user, ChatStatus.START);
        sendMessage.setReplyMarkup(Buttons.codeButtons());
        botService.sendAndSave(sendMessage, user, BotMessageType.HELLO, true);
    }

    public User addNewUser(Update update, long chatId) {
        String username = update.getMessage().getFrom().getUserName();
        if (username == null || username.isEmpty()) {
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            username = (firstName + (lastName == null ? "" : "_" + lastName))
                    .trim().replace(" ", "_");
        }
        User protoUser = User.newBuilder()
                .setUsername(username)
                .setChatId(chatId)
                .build();
        ResponseUser response = grpcUserStub.addUser(protoUser);

        if (response.hasUser()) {

            BetSendMessage sendMessage = new BetSendMessage(chatId);
            sendMessage.setText("Привет! \nВаш username: " + response.getUser().getUsername() +
                    "\nВаш код: " + response.getUser().getCode() + "\nInfo о боте: /info");
            sendMessage.setReplyMarkup(closeButton());
            botService.sendAndSave(sendMessage, response.getUser(), BotMessageType.START_MESSAGE);
            return response.getUser();
        }
        throw new IllegalStateException("Неожиданная ошибка при добавлении нового пользователя");
    }

    public void getCode(long chatId) {
        ResponseUser response = grpcUserStub.getUser(User.newBuilder().setChatId(chatId).build());
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        if (response.hasUser()) {
            sendMessage.setText("Ваш код: " + response.getUser().getCode());

        } else {
            sendMessage.setText("Вашего кода еще не существует. Нажмите /start");
        }
        botService.sendAndSave(sendMessage, response.getUser(), BotMessageType.CODE, true);
    }
}
