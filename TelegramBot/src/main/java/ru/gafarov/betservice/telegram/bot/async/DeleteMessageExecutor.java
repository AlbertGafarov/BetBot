package ru.gafarov.betservice.telegram.bot.async;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class DeleteMessageExecutor extends ThreadPoolTaskExecutor {
}
