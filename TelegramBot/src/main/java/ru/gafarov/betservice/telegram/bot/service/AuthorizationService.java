package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.*;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;

import static ru.gafarov.betservice.telegram.bot.components.Buttons.closeButton;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final UserService userService;
    private final BotMessageService botMessageService;
    private final BotService botService;

    public void authorization(Update update) {
        long chatId = update.getMessage().getChatId();
        ResponseUser responseMessage = grpcStub.getUser(User.newBuilder().setChatId(chatId).build());
        if (!responseMessage.hasUser()) {
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
            ResponseUser response = grpcStub.addUser(protoUser);

            if (response.hasUser()) {
                BetSendMessage sendMessage = new BetSendMessage(chatId);
                sendMessage.setText("Привет! \nВаш username: " + response.getUser().getUsername() +
                        "\nВаш код: " + response.getUser().getCode());
                sendMessage.setReplyMarkup(closeButton());
                botService.sendAndSave(sendMessage, response.getUser(), BotMessageType.INFO);
            }
        } else {

            botMessageService.deleteWithoutDraft(DraftBet.newBuilder().build(), responseMessage.getUser());
        }
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("Привет " + responseMessage.getUser().getUsername() + "!");
        userService.setChatStatus(responseMessage.getUser(), ChatStatus.START);
        sendMessage.setReplyMarkup(Buttons.codeButtons());
        botService.sendAndSave(sendMessage, responseMessage.getUser(), BotMessageType.HELLO);
    }

    public void getCode(long chatId) {
        ResponseUser response = grpcStub.getUser(User.newBuilder().setChatId(chatId).build());
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        if (response.hasUser()) {
            sendMessage.setText("Ваш код: " + response.getUser().getCode());

        } else {
            sendMessage.setText("Вашего кода еще не существует. Нажмите /start");
        }
        botService.sendAndSave(sendMessage, response.getUser(), BotMessageType.CODE);
    }
}
