package ru.gafarov.betservice.telegram.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.telegram.bot.components.Buttons;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;
import ru.gafarov.betservice.telegram.bot.prettyPrint.PrettyPrinter;
import ru.gafarov.betservice.telegram.bot.service.AuthorizationService;
import ru.gafarov.betservice.telegram.bot.service.BetService;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;
import ru.gafarov.betservice.telegram.bot.service.UserService;

import java.util.Arrays;

import static ru.gafarov.betservice.telegram.bot.components.BotCommands.LIST_OF_COMMANDS;

@Slf4j
@Component
public class BetTelegramBot extends TelegramLongPollingBot {

    private final ConfigMap configMap;
    private final AuthorizationService authorizationService;
    private final UserService userService;
    private final DraftBetService draftBetService;
    private final BetService betService;
    private final PrettyPrinter prettyPrinter;

    public BetTelegramBot(ConfigMap configMap, AuthorizationService authorizationService, UserService userService
            , DraftBetService draftBetService, BetService betService, PrettyPrinter prettyPrinter) {
        this.configMap = configMap;
        this.authorizationService = authorizationService;
        this.userService = userService;
        this.draftBetService = draftBetService;
        this.betService = betService;
        this.prettyPrinter = prettyPrinter;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasMessage()) {
                log.info("text: {}", update.getMessage().getText());
                log.info(update.getMessage().getFrom().getUserName());
                log.info(update.getMessage().getFrom().getFirstName());
                log.info(update.getMessage().getFrom().getLastName());

                long chatId = update.getMessage().getChatId();
                Proto.User user = userService.getUser(chatId);
                if (user == null || update.getMessage().getText().equals("/start")) {

                    execute(authorizationService.authorization(update));
                } else if (update.getMessage().getText().equals("/code")) {

                    execute(authorizationService.getCode(chatId));
                } else if (update.getMessage().getText().equals("/create")) {

                    Proto.DraftBet draftBet = Proto.DraftBet.newBuilder().build();
                    draftBet = draftBetService.saveDraftBet(draftBet);
                    user = user.toBuilder().setDraftBet(draftBet).build();
                    userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Введите username оппонента");
                    execute(sendMessage);
                } else {
                    if (Proto.ChatStatus.WAIT_OPPONENT_NAME.equals(user.getChatStatus())) {
                        String opponentName = update.getMessage().getText();
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setOpponentName(opponentName).build();
                        draftBetService.setOpponentName(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_CODE);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите code оппонента");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_OPPONENT_CODE.equals(user.getChatStatus())) {
                        int opponentCode = Integer.parseInt(update.getMessage().getText());
                        Proto.User opponent = userService.getUser(user.getDraftBet().getOpponentName(), opponentCode);
                        if (opponent == null) {
                            userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText(" Код не соответствует username. Введите username оппонента");
                            execute(sendMessage);
                        } else {
                            Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setOpponentCode(opponentCode).build();
                            draftBetService.setOpponentCode(draftBet);
                            user = user.toBuilder().setDraftBet(draftBet).build();
                            userService.setChatStatus(user, Proto.ChatStatus.WAIT_DEFINITION);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Введите суть спора");
                            execute(sendMessage);
                        }
                    } else if (Proto.ChatStatus.WAIT_DEFINITION.equals(user.getChatStatus())) {
                        String definition = update.getMessage().getText();
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setDefinition(definition).build();
                        draftBetService.setDefinition(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        userService.setChatStatus(user, Proto.ChatStatus.WAIT_WAGER);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите вознаграждение");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
                        String wager = update.getMessage().getText();
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setWager(wager).build();
                        draftBetService.setWager(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        userService.setChatStatus(user, Proto.ChatStatus.WAIT_FINISH_DATE);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите количество дней до завершения спора");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_FINISH_DATE.equals(user.getChatStatus())) {
                        int setDaysToFinish = Integer.parseInt(update.getMessage().getText());
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setDaysToFinish(setDaysToFinish).build();
                        draftBet = draftBetService.setDaysToFinish(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        userService.setChatStatus(user, Proto.ChatStatus.WAIT_APPROVE);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        String stringBuilder = "Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) +
                                "\nПодтверждаете?";
                        sendMessage.setText(stringBuilder);
                        sendMessage.setReplyMarkup(Buttons.okAndCancelButtons());
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus()) && update.getMessage().getText().equals("/draft")) {
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().build();
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        String stringBuilder = "Новый спор:\n" + prettyPrinter.printDraftBet(draftBet) +
                                "\nПодтверждаете?";
                        sendMessage.setText(stringBuilder);
                        sendMessage.setReplyMarkup(Buttons.okAndCancelButtons());
                        execute(sendMessage);
                    }
                }

            } else if (update.hasCallbackQuery()) {
                long chatId = update.getCallbackQuery().getFrom().getId();
                log.info("Получена команда от {}: {}", chatId, update.getCallbackQuery().getData());
                String[] command = update.getCallbackQuery().getData().split("/");
                log.info("Команда разбита на части: {}", Arrays.toString(command));
                if ("code".equals(command[1])) {
                    execute(authorizationService.getCode(chatId));
                }
                Proto.User user = userService.getUser(chatId);
                if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
                    Proto.DraftBet draftBet = user.getDraftBet();
                    log.info("Подготовленный спор: {}", draftBet);
                    switch (command[1]) {
                        case "ok":
                            Proto.Bet bet = betService.addBet(draftBet, user);
                            userService.setChatStatus(user, Proto.ChatStatus.START);

                            // Предложение оппоненту нового спора
                            SendMessage offerToOpponent = new SendMessage();
                            offerToOpponent.setChatId(bet.getOpponent().getChatId());
                            offerToOpponent.setText(prettyPrinter.printOfferBet(bet));
                            offerToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
                            offerToOpponent.setParseMode(ParseMode.HTML);
                            execute(offerToOpponent);

                            // Подтверждение инициатору об отправке
                            SendMessage msgDeliveryToInitiator = new SendMessage();
                            msgDeliveryToInitiator.setChatId(bet.getInitiator().getChatId());
                            msgDeliveryToInitiator.setText("Предложение о споре отправлено оппоненту");
                            msgDeliveryToInitiator.setParseMode(ParseMode.HTML);
                            execute(msgDeliveryToInitiator);

                            break;
                        case "cancel":
                            userService.setChatStatus(user, Proto.ChatStatus.START);
                            break;
                    }
                }
                if ("newStatus".equals(command[1])) {
                    long betId = Long.parseLong(command[3]);
                    Proto.ResponseMessage response = betService.setStatus(user, betId, Proto.BetStatus.valueOf(command[2]));
                    Proto.Bet bet = response.getBet();

                    // Оповещение оппонента если оно есть
                    if (!response.getMessageForOpponent().isEmpty()) {
                        SendMessage msgToOpponent = new SendMessage();
                        msgToOpponent.setChatId(bet.getOpponent().getChatId());
                        msgToOpponent.setText(response.getMessageForOpponent());
                        msgToOpponent.setParseMode(ParseMode.HTML);
                        execute(msgToOpponent);

                        SendMessage msgBetToOpponent = new SendMessage();
                        msgBetToOpponent.setChatId(bet.getOpponent().getChatId());
                        msgBetToOpponent.setText(prettyPrinter.printBet(bet));
                        msgBetToOpponent.setReplyMarkup(Buttons.nextStatusesButtons(bet.getOpponentNextStatusesList(), bet.getId()));
                        msgBetToOpponent.setParseMode(ParseMode.HTML);
                        execute(msgBetToOpponent);
                    }
                    // Оповещение инициатора если оно есть
                    if (!response.getMessageForInitiator().isEmpty()) {
                        SendMessage msgToInitiator = new SendMessage();
                        msgToInitiator.setChatId(bet.getInitiator().getChatId());
                        msgToInitiator.setText(response.getMessageForInitiator());
                        msgToInitiator.setParseMode(ParseMode.HTML);
                        execute(msgToInitiator);

                        SendMessage msgBetToInitiator = new SendMessage();
                        msgBetToInitiator.setChatId(bet.getInitiator().getChatId());
                        msgBetToInitiator.setText(prettyPrinter.printBet(bet));
                        msgBetToInitiator.setReplyMarkup(Buttons.nextStatusesButtons(bet.getInitiatorNextStatusesList(), bet.getId()));
                        msgBetToInitiator.setParseMode(ParseMode.HTML);
                        execute(msgBetToInitiator);
                    }
                }
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
