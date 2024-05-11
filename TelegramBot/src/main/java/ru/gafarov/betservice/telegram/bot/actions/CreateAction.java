package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.DrBetServiceGrpc;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import static ru.gafarov.betservice.telegram.bot.components.buttons.Buttons.wantChoseFromFriends;

@Component
@RequiredArgsConstructor
public class CreateAction implements Action {

    private final UserService userService;
    private final BotService botService;
    private final BotMessageService botMessageService;
    private final DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        DraftBet draftBet = DraftBet.newBuilder()
                .setInitiator(user).build();
        draftBet = grpcDrBetStub.addDraftBet(draftBet).getDraftBet();
        userService.setChatStatus(user, ChatStatus.WAIT_OPPONENT_NAME);
        BetSendMessage sendMessage = new BetSendMessage(chatId);
        sendMessage.setText("Введите username оппонента");
        if (!userService.getSubscribes(user).isEmpty()) {
            sendMessage.setReplyMarkup(wantChoseFromFriends(draftBet));
        }
        botMessageService.deleteWithoutDraft(draftBet, user);
        botService.sendAndSaveDraftBet(sendMessage, user, BotMessageType.ENTER_USERNAME, draftBet);
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {

        String[] command = update.getCallbackQuery().getData().split("/");
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);
        // /create/bet/with/{id}
        if ("with".equals(command[3])) {
            BetSendMessage sendMessage = new BetSendMessage(chatId); // Ответное сообщение
            User opponent = userService.findFriendById(user, Long.parseLong(command[4]));
            if (opponent != null) {
                DraftBet draftBet = DraftBet.newBuilder()
                        .setInitiator(user)
                        .setOpponentName(opponent.getUsername())
                        .setOpponentCode(opponent.getCode())
                        .setInverseDefinition(true).build();
                draftBet = grpcDrBetStub.addDraftBet(draftBet).getDraftBet();
                userService.setChatStatus(user, ChatStatus.WAIT_DEFINITION);
                sendMessage.setText(String.format("Новый спор с %s %s\n Введите суть спора", opponent.getUsername(), opponent.getCode()));
                botService.sendAndSaveDraftBet(sendMessage, user, BotMessageType.ENTER_DEFINITION, draftBet);
            }
        }
    }
}
