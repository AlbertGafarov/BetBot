package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.*;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.DrBet.ResponseDraftBet;
import ru.gafarov.bet.grpcInterface.Friend.Subscribe;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ResponseUser;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.gafarov.betservice.telegram.bot.components.buttons.Buttons.closeButton;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final FriendServiceGrpc.FriendServiceBlockingStub grpcFriendStub;
    private final UserServiceGrpc.UserServiceBlockingStub grpcUserStub;
    private final DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub;
    private final SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcSecretKeyStub;
    private final BotService botService;
    private final BotMessageService botMessageService;

    public User getUser(long chatId) {
        ResponseUser responseMessage = grpcUserStub.getUser(User.newBuilder().setChatId(chatId).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        } else if (Status.NOT_FOUND.equals(responseMessage.getStatus())) {
            return null;
        }
        throw new IllegalStateException("Получена неожиданная ошибка при поиске пользователя");
    }

    public User getUserById(long id) {
        ResponseUser responseMessage = grpcUserStub.getUser(User.newBuilder().setId(id).build());
        if (responseMessage.hasUser()) {
            return responseMessage.getUser();
        }
        return null;
    }

    public void setChatStatus(User protoUser, ChatStatus chatStatus) {
        setChatStatus(protoUser, chatStatus, null);
        log.info("Для пользователя {} установлен статус {}", protoUser.getChatId(), chatStatus);
    }

    public void setChatStatus(User protoUser, ChatStatus chatStatus, Long betId) {
        val builder = protoUser.getDialogStatus().toBuilder()
                .setChatStatus(chatStatus);
        if (betId != null) {
            builder.setBetId(betId);
        }
        protoUser = User.newBuilder(protoUser)
                .setDialogStatus(builder.build())
                .build();
        log.debug("Меняем статус чата: \n{}", protoUser);
        ResponseUser response = grpcUserStub.changeChatStatus(protoUser);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке установить статус пользователя");
        }
    }

    public User getUser(String username, int code) {
        ResponseUser response = grpcUserStub.getUser(User.newBuilder()
                .setUsername(username)
                .setCode(code).build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public DraftBet getLastDraftBet(User user) {
        ResponseDraftBet response = grpcDrBetStub.getLastDraftBet(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }

    public User findFriendByChatId(User subscriber, long chatId) {
        ResponseUser response = grpcFriendStub.findFriend(Subscribe.newBuilder()
                .setSubscriber(subscriber)
                .setSubscribed(User.newBuilder().setChatId(chatId).build())
                .build());
        if (response.hasUser()) {
            return response.getUser();
        }
        return null;
    }

    public User findFriendById(User subscriber, long friendId) {
        ResponseUser response = grpcFriendStub.findFriend(Subscribe.newBuilder()
                .setSubscriber(subscriber)
                .setSubscribed(User.newBuilder().setId(friendId).build())
                .build());
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getUser();
        }
        return null;
    }

    public List<User> getSubscribes(User user) {
        ResponseUser response = grpcFriendStub.getSubscribes(user);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getUsersList();
        } else if (response.getStatus().equals(Status.NOT_FOUND)) {
            return new ArrayList<>();
        }
        log.error("Получена ошибка при попытке получения списка друзей");
        return null;
    }

    public void saveMessageWithKey(User user, Update update) {
        Pattern pattern = Pattern.compile("[\\d\\D]{3,24}");
        if (!pattern.matcher(update.getMessage().getText().trim()).matches()) {
            log.error("Введено недопустимое значение для секрета. Доступны только латинские буквы и цифры от 3-х до 24-х символов");
            BetSendMessage sendInfoMessage = new BetSendMessage(user.getChatId());
            sendInfoMessage.setText("Получена ошибка. Попробуйте еще раз");
            sendInfoMessage.setReplyMarkup(closeButton());
            botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ENTER_SECRET_KEY);
            botService.sendAndSave(sendInfoMessage, user, BotMessageOuterClass.BotMessageType.ERROR_SECRET_KEY, true);
            return;
        }
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(user.getChatId());
        forwardMessage.setMessageId(update.getMessage().getMessageId());
        forwardMessage.setFromChatId(user.getChatId());
        Message message = botService.forward(forwardMessage);
        Integer id = message.getMessageId();
        SecretKey.MessageWithKey messageWithKey = SecretKey.MessageWithKey.newBuilder()
                .setUser(user)
                .setTgMessageId(id)
                .setSecretKey(message.getText().trim()).build();
        Rs.Response response = grpcSecretKeyStub.saveMessageWithKey(messageWithKey);
        if (Status.SUCCESS.equals(response.getStatus())) {
            BetSendMessage sendInfoMessage = new BetSendMessage(user.getChatId());
            sendInfoMessage.setText("Секретный ключ получен. Мы не сохраняем его у себя, поэтому запомните его или не удаляйте из этого чата");
            sendInfoMessage.setDelTime(60_000);
            botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ENTER_SECRET_KEY);
            botService.sendAndSave(sendInfoMessage, user, BotMessageOuterClass.BotMessageType.SECRET_KEY_SAVED, true);
        } else {
            log.error("Получена ошибка при попытке сохранить номер сообщения");
            BetSendMessage sendInfoMessage = new BetSendMessage(user.getChatId());
            sendInfoMessage.setText("Получена ошибка. Попробуйте еще раз");
            sendInfoMessage.setReplyMarkup(closeButton());
            botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ENTER_SECRET_KEY);
            botService.sendAndSave(sendInfoMessage, user, BotMessageOuterClass.BotMessageType.ERROR_SECRET_KEY, true);
        }
    }

    public User setEncryptionStatus(User user, boolean encryptionStatus) {
        user = user.toBuilder().setEncryptionEnabled(encryptionStatus).build();
        ResponseUser response = grpcUserStub.setEncryptionStatus(user);
        user = response.getUser();
        if (!Status.SUCCESS.equals(response.getStatus())) {
            log.error("Получена ошибка при попытке изменить статус шифрования");
        }
        return user;
    }

    public void reSaveMessageWithKey(User user, Update update) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(user.getChatId());
        forwardMessage.setMessageId(update.getMessage().getMessageId());
        forwardMessage.setFromChatId(user.getChatId());
        Message message = botService.forward(forwardMessage);
        Integer id = message.getMessageId();
        SecretKey.MessageWithKey messageWithKey = SecretKey.MessageWithKey.newBuilder()
                .setUser(user)
                .setTgMessageId(id)
                .setSecretKey(message.getText()).build();
        Rs.Response response = grpcSecretKeyStub.reSaveMessageWithKey(messageWithKey);
        if (Status.SUCCESS.equals(response.getStatus())) {
            BetSendMessage sendInfoMessage = new BetSendMessage(user.getChatId());
            sendInfoMessage.setText("Секретный ключ получен. Мы не сохраняем его у себя, поэтому запомните его или не удаляйте из этого чата");
            sendInfoMessage.setDelTime(60_000);
            botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ENTER_SECRET_KEY);
            botService.sendAndSave(sendInfoMessage, user, BotMessageOuterClass.BotMessageType.SECRET_KEY_SAVED, true);
            setChatStatus(user, ChatStatus.START);
        } else {
            log.error("Код не совпадает с ранее введенным кодом, chatId: {}", user.getChatId());
            BetSendMessage sendInfoMessage = new BetSendMessage(user.getChatId());
            sendInfoMessage.setText("Введен неверный код. Попробуйте еще раз");
            sendInfoMessage.setReplyMarkup(closeButton());
            botMessageService.deleteByBotMessageType(user, BotMessageOuterClass.BotMessageType.ENTER_SECRET_KEY);
            botService.sendAndSave(sendInfoMessage, user, BotMessageOuterClass.BotMessageType.ERROR_SECRET_KEY, true);
        }
    }
}