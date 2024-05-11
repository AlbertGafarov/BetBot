package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;


@Slf4j
@Service
@RequiredArgsConstructor
public class DialogAction {

    private final UserService userService;
    private final BotService botService;
    private final DraftBetService draftBetService;
    private final BetService betService;

    public void readMessageAndAction(Update update) {

        Message message = update.getMessage();
        User user = userService.getUser(message.getChatId());

        String text = message.getText().trim(); // Текст сообщения
        BetSendMessage replyMessage = new BetSendMessage(message.getChatId()); // Ответное сообщение
        EditMessageText editMessageText = new EditMessageText(); // Сообщение для редактирования
        editMessageText.setChatId(message.getChatId());

        ChatStatus userChatStatus = user.getDialogStatus().getChatStatus();

        val botMessageBuilder = BotMessage.newBuilder().setUser(user);
        if (ChatStatus.START.equals(userChatStatus) && message.getForwardFrom() != null) {

            draftBetService.createDraftBetFromFoward(user, message, text, replyMessage);
            userService.setChatStatus(user, ChatStatus.WAIT_WAGER);
        } else if (ChatStatus.WAIT_OPPONENT_NAME.equals(userChatStatus)) {

            draftBetService.setOpponentNameToDraftBet(user, text, editMessageText, botMessageBuilder, replyMessage);
            userService.setChatStatus(user, ChatStatus.WAIT_OPPONENT_CODE);
        } else if (ChatStatus.WAIT_OPPONENT_CODE.equals(userChatStatus)) {

            draftBetService.setOpponentCodeToDraftBet(user, text, replyMessage, editMessageText, botMessageBuilder);
            userService.setChatStatus(user, ChatStatus.WAIT_DEFINITION);
        } else if (ChatStatus.WAIT_DEFINITION.equals(userChatStatus)) {

            draftBetService.setDefinitionToDraftBet(user, text, botMessageBuilder, editMessageText, replyMessage);
            userService.setChatStatus(user, ChatStatus.WAIT_WAGER);
        } else if (ChatStatus.WAIT_WAGER.equals(userChatStatus)) {

            draftBetService.setWagerToDraftBet(user, text, editMessageText, botMessageBuilder, replyMessage);
            userService.setChatStatus(user, ChatStatus.WAIT_FINISH_DATE);
        } else if (ChatStatus.WAIT_FINISH_DATE.equals(userChatStatus)) {

            draftBetService.setFinishDateToDraftBet(user, text, editMessageText, botMessageBuilder, replyMessage);
            userService.setChatStatus(user, ChatStatus.WAIT_APPROVE);
        } else if (ChatStatus.WAIT_ARGUMENT.equals(userChatStatus)) {
            betService.addArgument(user, text);
            userService.setChatStatus(user, ChatStatus.START);
        }
        botService.delete(update);
    }
}
