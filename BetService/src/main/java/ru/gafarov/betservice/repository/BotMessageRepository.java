package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.model.BotMessage;

public interface BotMessageRepository  extends JpaRepository<BotMessage, Long> {
}
