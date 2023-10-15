package ru.gafarov.betservice.telegram.bot.actions;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Action {
    List<SendMessage> handle(Update update);
    List<SendMessage> callback(Update update);
}
