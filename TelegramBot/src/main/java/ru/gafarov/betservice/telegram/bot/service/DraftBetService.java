package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.DrBet.ResponseDraftBet;
import ru.gafarov.bet.grpcInterface.DrBetServiceGrpc;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.buttons.BetButtons;
import ru.gafarov.betservice.telegram.bot.components.buttons.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;


@Slf4j
@Service
@RequiredArgsConstructor
public class DraftBetService {

    private final UserService userService;
    private final DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;
    private final BetService betService;

    public void setFinishDateToDraftBet(User user, String text, EditMessageText editMessageText
            , BotMessage.Builder botMessageBuilder, BetSendMessage replyMessage) {
        DraftBet draftBet = userService.getLastDraftBet(user);
        try {
            if (text.length() > 5) {
                throw new NumberFormatException();
            }
            int setDaysToFinish = Integer.parseInt(text);
            if (setDaysToFinish > 25000) {
                throw new NumberFormatException();
            }
            draftBet = grpcDrBetStub.setFinishDate(draftBet.toBuilder().setDaysToFinish(setDaysToFinish).build()).getDraftBet();

            // Заполняем поле WAIT_FINISH_DATE
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_FINISH_DATE).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите количество дней до завершения спора: " + setDaysToFinish);

            botService.edit(editMessageText);

            replyMessage.setText("Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) + "\nПодтверждаете?");
            replyMessage.setReplyMarkup(Buttons.approveDraftBetButtons(draftBet.getId()));
            botService.sendAndSave(replyMessage, user, BotMessageType.DRAFT_BET, draftBet);

        } catch (NumberFormatException e) {
            replyMessage.setText("Введите количество дней до завершения спора. Это должно быть натуральное число не более 25000");
            botService.sendAndSave(replyMessage, user, BotMessageType.WRONG_FINISH_DATE, draftBet);
        }
    }

    public void setWagerToDraftBet(User user, String text, EditMessageText editMessageText
            , BotMessage.Builder botMessageBuilder, BetSendMessage replyMessage) {
        DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setWager(text).build();
        log.debug("Сохраняем вознаграждение в черновике запроса \n{}", draftBet);
        ResponseDraftBet response = grpcDrBetStub.setWager(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке сохранить вознаграждение в черновике запроса с id: {}", draftBet.getId());
        }

        // Заполняем поле WAIT_WAGER
        editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                .setType(BotMessageType.ENTER_WAGER).setDraftBet(draftBet).build()));
        editMessageText.setText("Введите вознаграждение: " + text);
        botService.edit(editMessageText);

        replyMessage.setText("Введите количество дней до завершения спора");
        botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_FINISH_DATE, draftBet);
    }

    public void setDefinitionToDraftBet(User user, String text, BotMessage.Builder botMessageBuilder
            , EditMessageText editMessageText, BetSendMessage replyMessage) {
        DraftBet draftBet = userService.getLastDraftBet(user)
                .toBuilder().setDefinition(text).build();
        setDefinition(draftBet);

        // Заполняем поле DEFINITION

        BotMessage botMessage = botMessageBuilder.setType(BotMessageType.ENTER_DEFINITION).setDraftBet(draftBet).build();
        int id = botMessageService.getId(botMessage);

        editMessageText.setMessageId(id);
        editMessageText.setText("Введите суть спора: " + text);
        botService.edit(editMessageText);

        replyMessage.setText("Введите вознаграждение");
        replyMessage.setReplyMarkup(Buttons.oneButton("без вознаграждения", "/draftBet/" + draftBet.getId() + "/withoutWager"));
        botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_WAGER, draftBet);
    }

    public void setOpponentCodeToDraftBet(User user, String text, BetSendMessage replyMessage
            , EditMessageText editMessageText, BotMessage.Builder botMessageBuilder) {
        User opponent = null;
        DraftBet draftBet = userService.getLastDraftBet(user);
        try {
            int opponentCode = Integer.parseInt(text);
            opponent = userService.getUser(draftBet.getOpponentName(), opponentCode);
        } catch (NumberFormatException e) {
            log.error("В ожидании кода введено не число");
        }
        if (opponent == null) {
            userService.setChatStatus(user, ChatStatus.WAIT_OPPONENT_NAME);
            replyMessage.setText(" Код не соответствует username. Введите username оппонента");
            botService.sendAndSave(replyMessage, user, BotMessageType.CODE_WRONG_ENTER_USERNAME, draftBet);

        } else {
            draftBet = draftBet.toBuilder().setOpponentCode(opponent.getCode()).build();
            setOpponentCodeAndName(draftBet);

            // Заполняем поле ENTER_CODE
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_CODE).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите code оппонента: " + opponent.getCode());
            botService.edit(editMessageText);

            replyMessage.setText("Введите суть спора");
            botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_DEFINITION, draftBet);
        }
    }

    public void setOpponentNameToDraftBet(User user, String text, EditMessageText editMessageText
            , BotMessage.Builder botMessageBuilder, BetSendMessage replyMessage) {
        DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setOpponentName(text).build();
        log.info("Сохраняем имя оппонента в черновике запроса \n{}", draftBet);
        ResponseDraftBet response = grpcDrBetStub.setOpponentName(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке сохранить имя оппонента в черновике запроса с id: {}", draftBet.getId());
        }
        // Заполняем поле ENTER_USERNAME
        editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                .setType(BotMessageType.ENTER_USERNAME).setDraftBet(draftBet).build()));
        editMessageText.setText("Введите username оппонента: " + text);

        botService.edit(editMessageText);
        // Отправляем ответное сообщение
        replyMessage.setText("Введите code оппонента");
        botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_CODE, draftBet);
    }

    public void createDraftBetFromFoward(User user, Message message, String text, BetSendMessage replyMessage) {
        User opponent = userService.findFriendByChatId(user, message.getForwardFrom().getId());
        if (opponent != null) {
            DraftBet draftBet = DraftBet.newBuilder().setDefinition(text).setInitiator(user)
                    .setOpponentName(opponent.getUsername())
                    .setOpponentCode(opponent.getCode())
                    .setInverseDefinition(true).build();
            draftBet = setDefinition(draftBet);

            replyMessage.setText(prettyPrinter.printDraftBetFromForwardMessage(draftBet));
            replyMessage.setReplyMarkup(Buttons.oneButton("без вознаграждения", "/draftBet/" + draftBet.getId() + "/withoutWager"));
            botService.sendAndSave(replyMessage, user, BotMessageType.ENTER_WAGER, draftBet);
        } else {
            log.warn("Не найден оппонент с chatId: {}", message.getForwardFrom().getId());
            replyMessage.setText("Спор не будет создан, потому что ваш оппонент не найден. " +
                    "Перешлите ему ссылку на этого бота, чтобы он мог подписаться");
            replyMessage.setDelTime(10000);
        }
    }

    public void setOpponentCodeAndName(DraftBet draftBet) {
        ResponseDraftBet response = grpcDrBetStub.setOpponentCode(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке сохранить код оппонента в черновике запроса с id: {}", draftBet.getId());
        }
    }

    public DraftBet setDefinition(DraftBet draftBet) {
        log.debug("Сохраняем суть в черновике запроса \n{}", draftBet.toString());
        ResponseDraftBet response = grpcDrBetStub.setDefinition(draftBet);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        } else {
            log.error("Получена ошибка при попытке сохранить суть спора: \n{}", draftBet);
            return null;
        }
    }

    public DraftBet getByIdAndUser(Long id, User user) {
        DraftBet protoDraft = DraftBet.newBuilder().setId(id).setInitiator(user).build();
        ResponseDraftBet response = grpcDrBetStub.getDraftBet(protoDraft);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        } else {
            log.error("Получена ошибка при попытке получить черновик с id: {}", id);
            return null;
        }
    }


    // /draftBet/{id}/approve/(ok|cancel)
    public void approveOrCancelDraft(Update update) {
        long chatId = update.getCallbackQuery().getFrom().getId();
        User user = userService.getUser(chatId);
        if (ChatStatus.WAIT_APPROVE.equals(user.getDialogStatus().getChatStatus())) {
            String[] command = update.getCallbackQuery().getData().split("/");
            DraftBet draftBet = getByIdAndUser(Long.valueOf(command[2]), user);
            log.debug("Подготовленный спор: {}", draftBet);

            botService.delete(update);
            userService.setChatStatus(user, ChatStatus.START);
            ResponseDraftBet response = grpcDrBetStub.deleteDraftBet(draftBet);
            if (!response.getStatus().equals(Status.SUCCESS)) {
                log.error("Получена ошибка при попытке удалить черновик запроса с id: {}", draftBet.getId());
            }
            botMessageService.deleteByDraft(draftBet, user);

            switch (command[4]) {
                case "ok":
                    ProtoBet.Bet bet = betService.addBet(draftBet, user);

                    // Предложение оппоненту нового спора
                    BetSendMessage offerToOpponent = new BetSendMessage(bet.getOpponent().getChatId());
                    offerToOpponent.setText(prettyPrinter.printOfferBet(bet));
                    offerToOpponent.setReplyMarkup(BetButtons.getBetButtons(bet, false));

                    // Подтверждение инициатору об отправке
                    BetSendMessage msgDeliveryToInitiator = new BetSendMessage(bet.getInitiator().getChatId());
                    msgDeliveryToInitiator.setText("Предложение о споре отправлено оппоненту");
                    msgDeliveryToInitiator.setDelTime(10_000);

                    botService.sendAndSave(offerToOpponent, bet.getOpponent(), BotMessageType.OFFER_BET);
                    botService.sendAndSave(msgDeliveryToInitiator, user, BotMessageType.APPROVE_DRAFT);
                    break;

                case "cancel":
                    BetSendMessage msgToInitiator = new BetSendMessage(chatId);
                    msgToInitiator.setText("Спор отклонен. Черновик удален");
                    msgToInitiator.setDelTime(10_000);

                    botService.sendAndSave(msgToInitiator, user, BotMessageType.CANCEL_DRAFT, draftBet);
                    break;
            }
        }
    }
}
