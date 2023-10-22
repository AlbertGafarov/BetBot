package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.Proto;
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
        long chatId = update.getMessage().getChatId();
        Proto.User user = userService.getUser(chatId);
        BetSendMessage sendMessage = new BetSendMessage();
        sendMessage.setChatId(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        val botMessageBuilder = Proto.BotMessage.newBuilder()
                .setUser(user);

        if (Proto.ChatStatus.WAIT_OPPONENT_NAME.equals(user.getChatStatus())) {
            String opponentName = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user).toBuilder().setOpponentName(opponentName).build();
            setOpponentName(draftBet);

            // Заполняем поле ENTER_USERNAME
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(Proto.BotMessageType.ENTER_USERNAME)
                    .setDraftBet(draftBet).build()));
            editMessageText.setText("Введите username оппонента: " + opponentName);
            botService.edit(editMessageText);

            userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_CODE);

            sendMessage.setText("Введите code оппонента");
            int id = botService.send(sendMessage);

            botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.ENTER_CODE)
                    .setDraftBet(draftBet).setTgMessageId(id).build());

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
                botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.CODE_WRONG_ENTER_USERNAME)
                        .setDraftBet(draftBet).setTgMessageId(id).build());

            } else {
                draftBet = draftBet.toBuilder().setOpponentCode(opponent.getCode()).build();
                setOpponentCode(draftBet);
                userService.setChatStatus(user, Proto.ChatStatus.WAIT_DEFINITION);

                // Заполняем поле ENTER_CODE
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(Proto.BotMessageType.ENTER_CODE)
                        .setDraftBet(draftBet).build()));
                editMessageText.setText("Введите code оппонента: " + opponent.getCode());
                botService.edit(editMessageText);

                sendMessage.setText("Введите суть спора");
                int id = botService.send(sendMessage);
                botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.ENTER_DEFINITION)
                        .setDraftBet(draftBet).setTgMessageId(id).build());
            }
        } else if (Proto.ChatStatus.WAIT_DEFINITION.equals(user.getChatStatus())) {
            String definition = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setDefinition(definition).build();
            setDefinition(draftBet);
            userService.setChatStatus(user, Proto.ChatStatus.WAIT_WAGER);

            // Заполняем поле ENTER_DEFINITION
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(Proto.BotMessageType.ENTER_DEFINITION)
                    .setDraftBet(draftBet).build()));
            editMessageText.setText("Введите суть спора: " + definition);
            botService.edit(editMessageText);

            sendMessage.setText("Введите вознаграждение");
            int id = botService.send(sendMessage);
            botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.ENTER_WAGER)
                    .setDraftBet(draftBet).setTgMessageId(id).build());

        } else if (Proto.ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
            String wager = update.getMessage().getText();
            Proto.DraftBet draftBet = userService.getLastDraftBet(user)
                    .toBuilder().setWager(wager).build();
            setWager(draftBet);
            userService.setChatStatus(user, Proto.ChatStatus.WAIT_FINISH_DATE);

            // Заполняем поле WAIT_WAGER
            editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                    .setType(Proto.BotMessageType.ENTER_WAGER)
                    .setDraftBet(draftBet).build()));
            editMessageText.setText("Введите вознаграждение: " + wager);
            botService.edit(editMessageText);

            sendMessage.setText("Введите количество дней до завершения спора");
            int id = botService.send(sendMessage);
            botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.ENTER_FINISH_DATE)
                    .setDraftBet(draftBet).setTgMessageId(id).build());

        } else if (Proto.ChatStatus.WAIT_FINISH_DATE.equals(user.getChatStatus())) {
            Proto.DraftBet draftBet = userService.getLastDraftBet(user);
            try {
                String text = update.getMessage().getText();
                if (text.length() > 5) {
                    throw new NumberFormatException();
                }
                int setDaysToFinish = Integer.parseInt(text);
                if(setDaysToFinish > 25000) {
                    throw new NumberFormatException();
                }
                draftBet = setDaysToFinish(draftBet.toBuilder().setDaysToFinish(setDaysToFinish).build());
                userService.setChatStatus(user, Proto.ChatStatus.WAIT_APPROVE);

                // Заполняем поле WAIT_FINISH_DATE
                editMessageText.setMessageId(botMessageService.getId(botMessageBuilder
                        .setType(Proto.BotMessageType.ENTER_FINISH_DATE)
                        .setDraftBet(draftBet).build()));
                editMessageText.setText("Введите количество дней до завершения спора: " + setDaysToFinish);

                botService.edit(editMessageText);

                sendMessage.setText("Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) + "\nПодтверждаете?");
                sendMessage.setReplyMarkup(Buttons.approveDraftBetButtons(draftBet.getId()));
                botService.send(sendMessage);
            } catch (NumberFormatException e) {
                sendMessage.setText("Введите количество дней до завершения спора. Это должно быть натуральное число не более 25000");
                int id = botService.send(sendMessage);
                botMessageService.save(botMessageBuilder.setType(Proto.BotMessageType.WRONG_FINISH_DATE)
                        .setDraftBet(draftBet).setTgMessageId(id).build());
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

    public Proto.DraftBet getByIdAndUser(Long id, Proto.User user) {
        Proto.DraftBet protoDraft = Proto.DraftBet.newBuilder().setId(id).setInitiator(user).build();
        Proto.ResponseDraftBet response = grpcStub.getDraftBet(protoDraft);
        if (response.getStatus().equals(Proto.Status.SUCCESS)) {
            return response.getDraftBet();
        }
        return null;
    }
}
