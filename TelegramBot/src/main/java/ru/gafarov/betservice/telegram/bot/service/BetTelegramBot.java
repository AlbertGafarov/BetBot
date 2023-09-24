package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetTelegramBot extends TelegramLongPollingBot {

    private final ConfigMap configMap;
    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        log.info(update.getMessage().getText());
        update.getMessage().getFrom().getUserName();
        Proto.User protoUser = Proto.User.newBuilder()
                .setName(update.getMessage().getFrom().getFirstName())
                .setChatId(chatId)
                .build();
        grpcStub.addUser(protoUser);
        try {
            if (update.getMessage().getText().equals("/start")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Привет " + update.getMessage().getFrom().getUserName());
                execute(sendMessage);
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotUsername() {
        return configMap.getBot().getName();
    }

    @Override
    public String getBotToken() {
        return configMap.getBot().getToken();
    }
}
