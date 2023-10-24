package ru.gafarov.betservice.telegram.bot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gafarov.betservice.telegram.bot.actions.Action;
import ru.gafarov.betservice.telegram.bot.components.BetCommands;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;
import ru.gafarov.betservice.telegram.bot.service.BotService;
import ru.gafarov.betservice.telegram.bot.service.DraftBetService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetTelegramBot extends TelegramLongPollingBot {

    public final static int READ_ONE_CHAR_MS = 50;
    public final static int WAIT_NEXT_MESSAGE_MS = 50;
    private final ConfigMap configMap;
    private final BetCommands betCommands;
    private final DraftBetService draftBetService;
    private final BotService botService;

    @Override
    public void onUpdateReceived(Update update) {
        Map<String, Action> actions = betCommands.getActions();
        if (update.hasMessage()) {

            String msgCommand = update.getMessage().getText();
            log.info("text: {}", msgCommand);
            if (actions.containsKey(msgCommand)) {
                log.info("Команда {} найдена", msgCommand);
                List<BetSendMessage> sendMessages = actions.get(msgCommand).handle(update);
                send(sendMessages);
            } else {
                draftBetService.createDraft(update);
            }

        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            log.info("Получена команда от {}: {}", chatId, update.getCallbackQuery().getData());
            String[] command = update.getCallbackQuery().getData().split("/");
            log.info("Команда разбита на части: {}", Arrays.toString(command));
            if (actions.containsKey(command[1])) {
                List<BetSendMessage> sendMessages = actions.get(command[1]).callback(update);
                send(sendMessages);
            }
        }
    }

    public void send(Collection<BetSendMessage> sendMessages) {
        try {
            for (BetSendMessage sendMessage : sendMessages) {
                int id = execute(sendMessage).getMessageId();
                if(sendMessage.getDelTime() > 0) {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setMessageId(id);
                    deleteMessage.setChatId(sendMessage.getChatId());
                    botService.delete(deleteMessage, sendMessage.getDelTime());
                }
                Thread.sleep(WAIT_NEXT_MESSAGE_MS);
            }
        } catch (TelegramApiException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void delete(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }
    @PostConstruct
    private void setBetCommands(){
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