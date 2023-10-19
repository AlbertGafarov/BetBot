package ru.gafarov.betservice.telegram.bot.actions;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

import java.util.List;

public interface Action {
    List<BetSendMessage> handle(Update update);
    List<BetSendMessage> callback(Update update);
}
