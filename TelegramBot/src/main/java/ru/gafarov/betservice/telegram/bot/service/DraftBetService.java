package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.controller.BetTelegramBot;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;

import java.util.List;

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
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        BetSendMessage sendMessage = new BetSendMessage();
        sendMessage.setChatId(chatId);

        if (Proto.ChatStatus.WAIT_OPPONENT_NAME.equals(user.getChatStatus())) {
            String opponentName = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setOpponentName(opponentName).build();
            setOpponentName(draftBet);

            // Заполняем поле ENTER_USERNAME
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(botMessageService.getId(Proto.BotMessage.newBuilder()
                    .setType(Proto.BotMessageType.ENTER_USERNAME)
                    .setDraftBet(draftBet)
                    .setUser(user)
                    .build()));
            editMessageText.setText("Введите username оппонента: " + opponentName);
            botService.edit(editMessageText);

            userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_CODE);

            sendMessage.setText("Введите code оппонента");
            int id = botService.send(sendMessage);

            botMessageService.save(Proto.BotMessage.newBuilder()
                    .setType(Proto.BotMessageType.ENTER_CODE).setDraftBet(draftBet)
                    .setTgMessageId(id).setUser(user).build());

        } else if (Proto.ChatStatus.WAIT_OPPONENT_CODE.equals(user.getChatStatus())) {
            Proto.User opponent = null;
            Proto.DraftBet draftBet = userService.getLastDraftBet(user);
            try {
                int opponentCode = Integer.parseInt(update.getMessage().getText());
                opponent = userService.getUser(draftBet.getOpponentName(), opponentCode);
            } catch (NumberFormatException e) {
                log.error("В ожидании кода введено не число");
            }
            if (opponent == null) {
                userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
                sendMessage.setText(" Код не соответствует username. Введите username оппонента");
                int id = botService.send(sendMessage);
                botMessageService.save(Proto.BotMessage.newBuilder()
                        .setType(Proto.BotMessageType.CODE_WRONG_ENTER_USERNAME).setDraftBet(draftBet)
                        .setTgMessageId(id).setUser(user).build());

            } else {
                draftBet = draftBet.toBuilder().setOpponentCode(opponent.getCode()).build();
                setOpponentCode(draftBet);
                userService.setChatStatus(user, Proto.ChatStatus.WAIT_DEFINITION);

                sendMessage.setText("Введите суть спора");
                int id = botService.send(sendMessage);
                botMessageService.save(Proto.BotMessage.newBuilder()
                        .setType(Proto.BotMessageType.ENTER_DEFINITION).setDraftBet(draftBet)
                        .setTgMessageId(id).setUser(user).build());
            }
        } else if (Proto.ChatStatus.WAIT_DEFINITION.equals(user.getChatStatus())) {
            String definition = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setDefinition(definition).build();
            setDefinition(draftBet);
            userService.setChatStatus(user, Proto.ChatStatus.WAIT_WAGER);

            sendMessage.setText("Введите вознаграждение");
            int id = botService.send(sendMessage);
            botMessageService.save(Proto.BotMessage.newBuilder()
                    .setType(Proto.BotMessageType.ENTER_WAGER).setDraftBet(draftBet)
                    .setTgMessageId(id).setUser(user).build());

        } else if (Proto.ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
            String wager = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setWager(wager).build();
            setWager(draftBet);
            userService.setChatStatus(user, Proto.ChatStatus.WAIT_FINISH_DATE);

            sendMessage.setText("Введите количество дней до завершения спора");
            int id = botService.send(sendMessage);
            botMessageService.save(Proto.BotMessage.newBuilder()
                    .setType(Proto.BotMessageType.ENTER_FINISH_DATE).setDraftBet(draftBet)
                    .setTgMessageId(id).setUser(user).build());

        } else if (Proto.ChatStatus.WAIT_FINISH_DATE.equals(user.getChatStatus())) {
            try {
                int setDaysToFinish = Integer.parseInt(update.getMessage().getText());
                Proto.DraftBet draftBet = userService.getLastDraftBet(user)
                        .toBuilder().setDaysToFinish(setDaysToFinish).build();
                draftBet = setDaysToFinish(draftBet);
                userService.setChatStatus(user, Proto.ChatStatus.WAIT_APPROVE);

                sendMessage.setText("Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) + "\nПодтверждаете?");
                sendMessage.setReplyMarkup(Buttons.approveDraftBetButtons());
                int id = botService.send(sendMessage);
            } catch (NumberFormatException e) {
                sendMessage.setText("Введите количество дней до завершения спора. Это должно быть натуральное число");
                int id = botService.send(sendMessage);
            }
        }

        botService.delete(update);
    }

    public Proto.DraftBet saveDraftBet(Proto.DraftBet draftBet) {
        log.info("Сохраняем черновик запроса \n{}", draftBet.toString());
        return grpcStub.addDraftBet(draftBet).getDraftBet();
    }

    public void setOpponentName(Proto.DraftBet draftBet) {
        log.info("Сохраняем имя оппонента в черновике запроса \n{}", draftBet.toString());
        grpcStub.setOpponentName(draftBet);
    }


    public void setOpponentCode(Proto.DraftBet draftBet) {
        log.info("Сохраняем код оппонента в черновике запроса \n{}", draftBet.toString());
        grpcStub.setOpponentCode(draftBet);
    }

    public void setDefinition(Proto.DraftBet draftBet) {
        log.info("Сохраняем суть в черновике запроса \n{}", draftBet.toString());
        grpcStub.setDefinition(draftBet);
    }

    public void setWager(Proto.DraftBet draftBet) {
        log.info("Сохраняем вознаграждение в черновике запроса \n{}", draftBet.toString());
        grpcStub.setWager(draftBet);
    }

    public Proto.DraftBet setDaysToFinish(Proto.DraftBet draftBet) {
        log.info("Сохраняем дату завершения спора в черновике запроса \n{}", draftBet.toString());
        return grpcStub.setFinishDate(draftBet).getDraftBet();
    }

    public void delete(Proto.DraftBet draftBet) {
        grpcStub.deleteDraftBet(draftBet);
    }
}
