package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.MessageWithKey;

import java.util.Optional;

public interface MessageWithKeyRepository extends JpaRepository<MessageWithKey, Long> {
    Optional<MessageWithKey> getByUserId(Long userId);
}