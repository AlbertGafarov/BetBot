package ru.gafarov.betservice.telegram.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.actions.Action;
import ru.gafarov.betservice.telegram.bot.components.BetCommands;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BetTelegramBot extends TelegramLongPollingBot {

    private final ConfigMap configMap;
    private final BetCommands betCommands;
    private final DraftBetService draftBetService;

    public BetTelegramBot(ConfigMap configMap, BetCommands betCommands, DraftBetService draftBetService) {
        this.configMap = configMap;
        this.betCommands = betCommands;
        this.draftBetService = draftBetService;
        try {
            this.execute(new SetMyCommands(betCommands.getBotCommandList(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Map<String, Action> actions = betCommands.getActions();
        if (update.hasMessage()) {
            String msgCommand = update.getMessage().getText();
            log.info("text: {}", msgCommand);
            if (actions.containsKey(msgCommand)) {
                log.info("Команда {} найдена", msgCommand);
                List<SendMessage> sendMessages = actions.get(msgCommand).handle(update);
                send(sendMessages);
            } else {
                List<SendMessage> sendMessages = draftBetService.createDraft(update);
                send(sendMessages);
            }

        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            log.info("Получена команда от {}: {}", chatId, update.getCallbackQuery().getData());
            String[] command = update.getCallbackQuery().getData().split("/");
            log.info("Команда разбита на части: {}", Arrays.toString(command));
            if (actions.containsKey(command[1])) {
                List<SendMessage> sendMessages = actions.get(command[1]).callback(update);
                send(sendMessages);
            }
        }
    }

    private void send(List<SendMessage> sendMessages) {
        try {
            for (SendMessage sendMessage : sendMessages) {
                execute(sendMessage);
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