package ru.gafarov.betservice.telegram.bot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetEditMessageText;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftAction implements Action {

    private final UserService userService;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;
    private final DraftBetService draftBetService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = userService.getUser(chatId);
        BetSendMessage sendMessage = new BetSendMessage(chatId);

        // /draft
        if (ChatStatus.WAIT_APPROVE.equals(user.getDialogStatus().getChatStatus())) {
            DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().build();
            String stringBuilder = "Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) +
                    "\nПодтверждаете?";
            sendMessage.setText(stringBuilder);
            sendMessage.setReplyMarkup(Buttons.approveDraftBetButtons(draftBet.getId()));

        botService.sendAndSave(sendMessage, user, BotMessageType.DRAFT_BET, true);
        } else {
            sendMessage.setText("У вас нет черновиков спора");
            sendMessage.setDelTime(5000);
        botService.sendAndSave(sendMessage, user, BotMessageType.YOU_HAVE_NOT_DRAFT_BET, true);
        }
        botService.delete(update);
    }

    @Override
    public void callback(Update update) {

        String[] command = update.getCallbackQuery().getData().split("/");
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);
        val botMessageBuilder = BotMessage.newBuilder().setUser(user);
        BetSendMessage replyMessage = new BetSendMessage(chatId); // Ответное сообщение

        // /draftBet/{id}/approve/(ok|cancel)
        switch (command[3]) {
            case "approve":
                draftBetService.approveOrCancelDraft(update);

                // /draftBet/{id}/setOpponent/{opponent_id}
                break;
            case "setOpponent": {
                User opponent = userService.getUserById(Long.parseLong(command[4]));
                DraftBet draftBet = draftBetService.getByIdAndUser(Long.parseLong(command[2]), user)
                        .toBuilder()
                        .setOpponentName(opponent.getUsername())
                        .setOpponentCode(opponent.getCode())
                        .build();
                draftBetService.setOpponentCodeAndName(draftBet);
                userService.setChatStatus(user, ChatStatus.WAIT_DEFINITION);

                //Деактивируем кнопки и редактируем сообщение
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                BetEditMessageText editMessageText = new BetEditMessageText(chatId, messageId);
                editMessageText.setText("Выбра(н/на): " + opponent.getUsername() + " " + opponent.getCode());
                botService.edit(editMessageText);

                replyMessage.setText("Введите суть спора");
                int tgMessageId = botService.sendTimeIsUpMessage(replyMessage);
                botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_DEFINITION)
                        .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());

                // /draftBet/{id}/showMyFriends
                break;
            }
            case "showMyFriends":

                BetSendMessage sendMessage = new BetSendMessage(chatId);

                List<User> friends = userService.getSubscribes(user);
                if (friends != null) {
                    List<List<InlineKeyboardButton>> rowsInLine = friends.stream().limit(100).map(a ->
                            (List<InlineKeyboardButton>) new ArrayList<InlineKeyboardButton>() {{
                                InlineKeyboardButton friend = new InlineKeyboardButton(a.getUsername() + " " + a.getCode());
                                friend.setCallbackData("/draftBet/" + command[2] + "/setOpponent/" + a.getId());
                                add(friend);
                            }}).collect(Collectors.toList());

                    InlineKeyboardMarkup friendButtons = new InlineKeyboardMarkup();
                    friendButtons.setKeyboard(rowsInLine);
                    sendMessage.setText("Выберите оппонента из списка");
                    sendMessage.setReplyMarkup(friendButtons);

                    //Деактивируем кнопку
                    EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                    editMessageReplyMarkup.setChatId(chatId);
                    editMessageReplyMarkup.setMessageId(botMessageService.getId(botMessageBuilder.setType(BotMessageType.ENTER_USERNAME)
                            .setDraftBet(DraftBet.newBuilder().setId(Integer.parseInt(command[2])).build()).build()));
                    editMessageReplyMarkup.setReplyMarkup(null);
                    botService.edit(editMessageReplyMarkup);

                    int tgMessageId = botService.sendTimeIsUpMessage(sendMessage);
                    // Сохраняем id сообщения в БД
                    botMessageService.save(botMessageBuilder
                            .setType(BotMessageType.CHOOSE_OPPONENT)
                            .setDraftBet(DraftBet.newBuilder().setId(Integer.parseInt(command[2])).build())
                            .setTgMessageId(tgMessageId)
                            .build());

                } else {
                    sendMessage.setText("У Вас пока нет друзей. Введите username перового друга:");
                    sendMessage.setDelTime(10000);

                    int tgMessageId = botService.sendAndDelete(sendMessage);
                    // Сохраняем id сообщения в БД
                    botMessageService.save(botMessageBuilder
                            .setType(BotMessageType.ENTER_USERNAME)
                            .setDraftBet(DraftBet.newBuilder().setId(Integer.parseInt(command[2])).build())
                            .setTgMessageId(tgMessageId)
                            .build());
                }
                // /draftBet/{id}/withoutWager
                break;
            case "withoutWager": {
                DraftBet draftBet = DraftBet.newBuilder().setId(Long.parseLong(command[2])).build();
                userService.setChatStatus(user, ChatStatus.WAIT_FINISH_DATE);

                // Заполняем поле WAIT_WAGER
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(BotMessageType.ENTER_WAGER).setDraftBet(draftBet).build()));
                editMessageText.setText("Введите вознаграждение: без вознаграждения");
                botService.edit(editMessageText);

                replyMessage.setText("Введите количество дней до завершения спора");
                botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_FINISH_DATE, draftBet);
                break;
            }
        }
    }
}
