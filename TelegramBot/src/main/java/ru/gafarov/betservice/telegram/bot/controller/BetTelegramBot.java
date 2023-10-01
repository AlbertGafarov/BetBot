package ru.gafarov.betservice.telegram.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
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

    public BetTelegramBot(ConfigMap configMap, AuthorizationService authorizationService, UserService userService, DraftBetService draftBetService, BetService betService, PrettyPrinter prettyPrinter) {
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
                    Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
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
                        Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_CODE);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите code оппонента");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_OPPONENT_CODE.equals(user.getChatStatus())) {
                        int opponentCode = Integer.parseInt(update.getMessage().getText());
                        Proto.User opponent = userService.getUser(user.getDraftBet().getOpponentName(), opponentCode);
                        if (opponent == null) {
                            Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_OPPONENT_NAME);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText(" Код не соответствует username. Введите username оппонента");
                            execute(sendMessage);
                        } else {
                            Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setOpponentCode(opponentCode).build();
                            draftBetService.setOpponentCode(draftBet);
                            user = user.toBuilder().setDraftBet(draftBet).build();
                            Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_DEFINITION);
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
                        Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_WAGER);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите вознаграждение");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_WAGER.equals(user.getChatStatus())) {
                        String wager = update.getMessage().getText();
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setWager(wager).build();
                        draftBetService.setWager(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_FINISH_DATE);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Введите количество дней до завершения спора");
                        execute(sendMessage);
                    } else if (Proto.ChatStatus.WAIT_FINISH_DATE.equals(user.getChatStatus())) {
                        int setDaysToFinish = Integer.parseInt(update.getMessage().getText());
                        Proto.DraftBet draftBet = user.getDraftBet().toBuilder().setDaysToFinish(setDaysToFinish).build();
                        draftBet = draftBetService.setDaysToFinish(draftBet);
                        user = user.toBuilder().setDraftBet(draftBet).build();
                        Proto.ChatStatus chatStatus1 = userService.setChatStatus(user, Proto.ChatStatus.WAIT_APPROVE);
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
                switch (update.getCallbackQuery().getData()) {
                    case "/code":
                        execute(authorizationService.getCode(chatId));
                        break;
                }
                Proto.User user = userService.getUser(chatId);
                if (Proto.ChatStatus.WAIT_APPROVE.equals(user.getChatStatus())) {
                    Proto.DraftBet draftBet = user.getDraftBet();
                    log.info("Подготовленный спор: {}", draftBet);
                    switch (update.getCallbackQuery().getData()) {
                        case "/ok":
                            Proto.Bet bet = betService.addBet(draftBet, user);
                            userService.setChatStatus(user, Proto.ChatStatus.START);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(bet.getOpponent().getChatId());
                            sendMessage.setText(prettyPrinter.printOfferBet(bet));
                            sendMessage.setReplyMarkup(Buttons.approveAndDeclineButtons(bet.getId()));
                            execute(sendMessage);
                            break;
                        case "/cancel":
                            userService.setChatStatus(user, Proto.ChatStatus.START);
                            break;
                    }
                    if(update.getCallbackQuery().getData().startsWith("/approveBet")) {
                        long betId = Long.parseLong(update.getCallbackQuery().getData().replace("/approveBet " , ""));
                        Proto.Bet bet = betService.setStatus(user, betId, Proto.BetStatus.ACCEPTED);

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(bet.getOpponent().getChatId());
                        sendMessage.setText(prettyPrinter.printBet(bet));
                        execute(sendMessage);
                    } else if(update.getCallbackQuery().getData().startsWith("/declineBet")) {
                        long betId = Long.parseLong(update.getCallbackQuery().getData().replace("/declineBet ", ""));
                        Proto.Bet bet = betService.setStatus(user, betId, Proto.BetStatus.CANCEL);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(bet.getOpponent().getChatId());
                        sendMessage.setText(prettyPrinter.printBet(bet));
                        execute(sendMessage);
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
