package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto.*;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftBetService {

    private final UserService userService;
    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
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
            User opponent = userService.getUser(message.getForwardFrom().getId());
            if (opponent != null) {
                DraftBet draftBet = DraftBet.newBuilder().setDefinition(text).setInitiator(user)
                        .setOpponentName(opponent.getUsername())
                        .setOpponentCode(opponent.getCode()).build();
                draftBet = setDefinition(draftBet);
                userService.setChatStatus(user, ChatStatus.WAIT_WAGER);

            replyMessage.setText(prettyPrinter.printDraftBetFromForward(draftBet));
            int tgMessageId = botService.send(replyMessage);
            botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_WAGER)
                    .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());
            } else {
                log.warn("Не найден оппонент с id: {}", message.getForwardFrom().getId());
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
            // Отправляем  ответное сообщение
            replyMessage.setText("Введите code оппонента");
            int tgMessageId = botService.send(replyMessage);
            // Сохраняем id сообщения в БД
            botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_CODE)
                    .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());

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
                int tgMessageId = botService.send(replyMessage);
                botMessageService.save(botMessageBuilder.setType(BotMessageType.CODE_WRONG_ENTER_USERNAME)
                        .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());

            } else {
                draftBet = draftBet.toBuilder().setOpponentCode(opponent.getCode()).build();
                setOpponentCode(draftBet);
                userService.setChatStatus(user, ChatStatus.WAIT_DEFINITION);

                // Заполняем поле ENTER_CODE
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(BotMessageType.ENTER_CODE).setDraftBet(draftBet).build()));
                editMessageText.setText("Введите code оппонента: " + opponent.getCode());
                botService.edit(editMessageText);

                replyMessage.setText("Введите суть спора");
                int tgMessageId = botService.send(replyMessage);
                botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_DEFINITION)
                        .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());
            }
        } else if (ChatStatus.WAIT_DEFINITION.equals(user.getChatStatus())) {

            DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setDefinition(text).build();
            setDefinition(draftBet);
            userService.setChatStatus(user, ChatStatus.WAIT_WAGER);

            // Заполняем поле ENTER_DEFINITION
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_DEFINITION).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите суть спора: " + text);
            botService.edit(editMessageText);

            replyMessage.setText("Введите вознаграждение");
            int tgMessageId = botService.send(replyMessage);
            botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_WAGER)
                    .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());

        } else if (ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
            DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setWager(text).build();
            setWager(draftBet);
            userService.setChatStatus(user, ChatStatus.WAIT_FINISH_DATE);

            // Заполняем поле WAIT_WAGER
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(BotMessageType.ENTER_WAGER).setDraftBet(draftBet).build()));
            editMessageText.setText("Введите вознаграждение: " + text);
            botService.edit(editMessageText);

            replyMessage.setText("Введите количество дней до завершения спора");
            int tgMessageId = botService.send(replyMessage);
            botMessageService.save(botMessageBuilder.setType(BotMessageType.ENTER_FINISH_DATE)
                    .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());

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
                botService.send(replyMessage);
            } catch (NumberFormatException e) {
                replyMessage.setText("Введите количество дней до завершения спора. Это должно быть натуральное число не более 25000");
                int tgMessageId = botService.send(replyMessage);
                botMessageService.save(botMessageBuilder.setType(BotMessageType.WRONG_FINISH_DATE)
                        .setDraftBet(draftBet).setTgMessageId(tgMessageId).build());
            }
        }

        botService.delete(update);
    }

    public DraftBet saveDraftBet(DraftBet draftBet) {
        log.info("Сохраняем черновик запроса \n{}", draftBet.toString());
        return grpcStub.addDraftBet(draftBet).getDraftBet();
    }

    public void setOpponentName(DraftBet draftBet) {
        log.info("Сохраняем имя оппонента в черновике запроса \n{}", draftBet.toString());
        grpcStub.setOpponentName(draftBet);
    }


    public void setOpponentCode(DraftBet draftBet) {
        log.info("Сохраняем код оппонента в черновике запроса \n{}", draftBet.toString());
        grpcStub.setOpponentCode(draftBet);
    }

    public DraftBet setDefinition(DraftBet draftBet) {
        log.info("Сохраняем суть в черновике запроса \n{}", draftBet.toString());
        ResponseDraftBet response =  grpcStub.setDefinition(draftBet);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }

    public void setWager(DraftBet draftBet) {
        log.info("Сохраняем вознаграждение в черновике запроса \n{}", draftBet.toString());
        grpcStub.setWager(draftBet);
    }

    public DraftBet setDaysToFinish(DraftBet draftBet) {
        log.info("Сохраняем дату завершения спора в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setFinishDate(draftBet).getDraftBet();
    }

    public void delete(DraftBet draftBet) {
        grpcStub.deleteDraftBet(draftBet);
    }

    public DraftBet getByIdAndUser(Long id, User user) {
        DraftBet protoDraft = DraftBet.newBuilder().setId(id).setInitiator(user).build();
        ResponseDraftBet response = grpcStub.getDraftBet(protoDraft);
        if (response.getStatus().equals(Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }
}
