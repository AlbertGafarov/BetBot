package ru.gafarov.betservice.telegram.bot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.actions.Action;
import ru.gafarov.betservice.telegram.bot.actions.DialogAction;
import ru.gafarov.betservice.telegram.bot.components.BetCommands;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetTelegramBot extends TelegramLongPollingBot {


    private final ConfigMap configMap;
    private final BetCommands betCommands;
    private final DialogAction dialogAction;

    @Override
    public void onUpdateReceived(Update update) {
        Map<String, Action> actions = betCommands.getActions();
        if (update.hasMessage()) {

            String[] command = update.getMessage().getText().split("/");
            log.info("Получено сообщение {} от {}. Разбито на части: {}", update.getUpdateId()
                    , update.getMessage().getFrom().getId(), Arrays.toString(command));
            if (command.length > 1 && actions.containsKey(command[1])) {
                log.debug("Команда {} найдена", "/" + command[1]);
                actions.get(command[1]).handle(update);
            } else {
                dialogAction.readMessageAndAction(update);
            }

        } else if (update.hasCallbackQuery()) {
            String[] command = update.getCallbackQuery().getData().split("/");
            log.info("Получена команда от {}. Разбита на части: {}", update.getCallbackQuery().getFrom().getId()
                    , Arrays.toString(command));
            if (command.length > 1 && actions.containsKey(command[1])) {
                log.debug("Команда {} найдена", "/" + command[1]);
                actions.get(command[1]).callback(update);
            }
        }
    }

    @PostConstruct
    private void setBetCommands() {
        try {
            this.execute(new SetMyCommands(betCommands.getBotCommandList(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
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