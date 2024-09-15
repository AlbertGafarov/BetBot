package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.MessageWithKey;

public interface MessageWithKeyRepository extends JpaRepository<MessageWithKey, Long> {
}