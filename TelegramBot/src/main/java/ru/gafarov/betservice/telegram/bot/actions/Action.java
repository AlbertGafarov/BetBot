package ru.gafarov.betservice.telegram.bot.actions;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Action {
    void handle(Update update);
    void callback(Update update);
}