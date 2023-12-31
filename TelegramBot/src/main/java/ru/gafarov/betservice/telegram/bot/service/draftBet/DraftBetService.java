package ru.gafarov.betservice.telegram.bot.service.draftBet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessage;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass.BotMessageType;
import ru.gafarov.bet.grpcInterface.DrBet.DraftBet;
import ru.gafarov.bet.grpcInterface.DrBet.ResponseDraftBet;
import ru.gafarov.bet.grpcInterface.DrBetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Rs.Status;
import ru.gafarov.bet.grpcInterface.UserOuterClass.ChatStatus;
import ru.gafarov.bet.grpcInterface.UserOuterClass.User;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.BotMessageService;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.UserService;


@Slf4j
@Service
@RequiredArgsConstructor
public class DraftBetService {

    private final UserService userService;
    private final DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub;
    private final PrettyPrinter prettyPrinter;
    private final BotService botService;
    private final BotMessageService botMessageService;

    public void createDraft(Update update) {

        Message message = update.getMessage();
        User user = userService.getUser(message.getChatId());

        String text = message.getText().trim(); // Текст сообщения
        BetSendMessage replyMessage = new BetSendMessage(message.getChatId()); // Ответное сообщение
        EditMessageText editMessageText = new EditMessageText(); // Сообщение для редактирования
        editMessageText.setChatId(message.getChatId());

        val botMessageBuilder = BotMessage.newBuilder().setUser(user);
        if (ChatStatus.START.equals(user.getChatStatus()) && message.getForwardFrom() != null) {
            User opponent = userService.findFriendByChatId(user, message.getForwardFrom().getId());
            if (opponent != null) {
                DraftBet draftBet = DraftBet.newBuilder().setDefinition(text).setInitiator(user)
                        .setOpponentName(opponent.getUsername())
                        .setOpponentCode(opponent.getCode())
                        .setInverseDefinition(true).build();
                draftBet = setDefinition(draftBet);
                userService.setChatStatus(user, ChatStatus.WAIT_WAGER);

                replyMessage.setText(prettyPrinter.printDraftBetFromForwardMessage(draftBet));
                replyMessage.setReplyMarkup(Buttons.oneButton("без вознаграждения", "/draftBet/" + draftBet.getId() + "/withoutWager"));
                botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.ENTER_WAGER, draftBet);
            } else {
                log.warn("Не найден оппонент с chatId: {}", message.getForwardFrom().getId());
                replyMessage.setText("Спор не будет создан, потому что ваш оппонент не найден. " +
                        "Перешлите ему ссылку на этого бота, чтобы он мог подписаться");
                replyMessage.setDelTime(10000);
            }
        } else if (ChatStatus.WAIT_OPPONENT_NAME.equals(user.getChatStatus())) {

            DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setOpponentName(text).build();
            setOpponentName(draftBet);
            // Заполняем поле ENTER_USERNAME
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_USERNAME).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите username оппонента: " + text);

            botService.edit(editMessageText);
            // Меняем статус чата
            userService.setChatStatus(user, ChatStatus.WAIT_OPPONENT_CODE);
            // Отправляем ответное сообщение
            replyMessage.setText("Введите code оппонента");
            botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.ENTER_CODE, draftBet);

        } else if (ChatStatus.WAIT_OPPONENT_CODE.equals(user.getChatStatus())) {

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
                botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.CODE_WRONG_ENTER_USERNAME, draftBet);

            } else {
                draftBet = draftBet.toBuilder().setOpponentCode(opponent.getCode()).build();
                setOpponentCodeAndName(draftBet);
                userService.setChatStatus(user, ChatStatus.WAIT_DEFINITION);

                // Заполняем поле ENTER_CODE
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(BotMessageType.ENTER_CODE).setDraftBet(draftBet).build()));
                editMessageText.setText("Введите code оппонента: " + opponent.getCode());
                botService.edit(editMessageText);

                replyMessage.setText("Введите суть спора");
                botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.ENTER_DEFINITION, draftBet);
            }
        } else if (ChatStatus.WAIT_DEFINITION.equals(user.getChatStatus())) {

            DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setDefinition(text).build();
            setDefinition(draftBet);
            userService.setChatStatus(user, ChatStatus.WAIT_WAGER);

            // Заполняем поле ENTER_DEFINITION

            BotMessage botMessage = botMessageBuilder.setType(BotMessageType.ENTER_DEFINITION).setDraftBet(draftBet).build();
            int id = botMessageService.getId(botMessage);
            System.out.println(draftBet.toString());
            System.out.println(id);

            editMessageText.setMessageId(id);
            editMessageText.setText("Введите суть спора: " + text);
            botService.edit(editMessageText);

            replyMessage.setText("Введите вознаграждение");
            replyMessage.setReplyMarkup(Buttons.oneButton("без вознаграждения", "/draftBet/" + draftBet.getId() + "/withoutWager"));
            botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.ENTER_WAGER, draftBet);

        } else if (ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
            DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setWager(text).build();
            setWager(draftBet);
            userService.setChatStatus(user, ChatStatus.WAIT_FINISH_DATE);

            // Заполняем поле WAIT_WAGER
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_WAGER).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите вознаграждение: " + text);
            botService.edit(editMessageText);

            replyMessage.setText("Введите количество дней до завершения спора");
            botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.ENTER_FINISH_DATE, draftBet);

        } else if (ChatStatus.WAIT_FINISH_DATE.equals(user.getChatStatus())) {
            DraftBet draftBet = userService.getLastDraftBet(user);
            try {
                if (text.length() > 5) {
                    throw new NumberFormatException();
                }
                int setDaysToFinish = Integer.parseInt(text);
                if (setDaysToFinish > 25000) {
                    throw new NumberFormatException();
                }
                draftBet = setDaysToFinish(draftBet.toBuilder().setDaysToFinish(setDaysToFinish).build());
                userService.setChatStatus(user, ChatStatus.WAIT_APPROVE);

                // Заполняем поле WAIT_FINISH_DATE
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(BotMessageType.ENTER_FINISH_DATE).setDraftBet(draftBet).build()));
                editMessageText.setText("Введите количество дней до завершения спора: " + setDaysToFinish);

                botService.edit(editMessageText);

                replyMessage.setText("Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) + "\nПодтверждаете?");
                replyMessage.setReplyMarkup(Buttons.approveDraftBetButtons(draftBet.getId()));
                botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.DRAFT_BET, draftBet);

            } catch (NumberFormatException e) {
                replyMessage.setText("Введите количество дней до завершения спора. Это должно быть натуральное число не более 25000");
                botService.sendAndSaveDraftBet(replyMessage, user, BotMessageType.WRONG_FINISH_DATE, draftBet);
            }
        }

        botService.delete(update);
    }

    public DraftBet saveDraftBet(DraftBet draftBet) {
        log.debug("Сохраняем черновик запроса \n{}", draftBet.toString());
        return grpcDrBetStub.addDraftBet(draftBet).getDraftBet();
    }

    public void setOpponentName(DraftBet draftBet) {
        log.info("Сохраняем имя оппонента в черновике запроса \n{}", draftBet.toString());
        ResponseDraftBet response = grpcDrBetStub.setOpponentName(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке сохранить имя оппонента в черновике запроса с id: {}", draftBet.getId());
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

    public void setWager(DraftBet draftBet) {
        log.debug("Сохраняем вознаграждение в черновике запроса \n{}", draftBet.toString());
        ResponseDraftBet response = grpcDrBetStub.setWager(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке сохранить вознаграждение в черновике запроса с id: {}", draftBet.getId());
        }
    }

    public DraftBet setDaysToFinish(DraftBet draftBet) {
        log.debug("Сохраняем дату завершения спора в черновике запроса \n{}", draftBet.toString());
        return grpcDrBetStub.setFinishDate(draftBet).getDraftBet();
    }

    public void delete(DraftBet draftBet) {
        ResponseDraftBet response = grpcDrBetStub.deleteDraftBet(draftBet);
        if (!response.getStatus().equals(Status.SUCCESS)) {
            log.error("Получена ошибка при попытке удалить черновик запроса с id: {}", draftBet.getId());
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
}
