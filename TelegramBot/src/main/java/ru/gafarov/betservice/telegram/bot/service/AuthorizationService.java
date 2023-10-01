package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.Buttons;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationService {

    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    public final UserService userService;

    public SendMessage authorization(Update update) {
        long chatId = update.getMessage().getChatId();
        Proto.ResponseMessage responseMessage = grpcStub.getUser(Proto.User.newBuilder().setChatId(chatId).build());
        if (!responseMessage.hasUser()) {
            String username = update.getMessage().getFrom().getUserName();
            if (username == null || username.isEmpty()) {
                String firstName = update.getMessage().getFrom().getFirstName();
                String lastName = update.getMessage().getFrom().getLastName();
                username = (firstName + (lastName == null ? "" : "_" + lastName))
                        .trim().replace(" ", "_");
            }
            Proto.User protoUser = Proto.User.newBuilder()
                    .setUsername(username)
                    .setChatId(chatId)
                    .build();
            Proto.ResponseMessage responseMessage1 = grpcStub.addUser(protoUser);

            if (responseMessage1.hasUser()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Привет! \nВаш username: " + responseMessage1.getUser().getUsername() +
                        "\nВаш код: " + responseMessage1.getUser().getCode() +
                        "\nимя и код нужно отправить вашему оппоненту");
                return sendMessage;
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Привет " + responseMessage.getUser().getUsername() + "!");
        userService.setChatStatus(responseMessage.getUser(), Proto.ChatStatus.START);
        sendMessage.setReplyMarkup(Buttons.inlineMarkup());
        return sendMessage;

    }

    public SendMessage getCode(long chatId) {
        Proto.ResponseMessage responseMessage = grpcStub.getUser(Proto.User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Ваш код: " + responseMessage.getUser().getCode());
            return sendMessage;
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Вашего кода еще не существует. Нажмите /start");
            return sendMessage;
        }
    }
}
