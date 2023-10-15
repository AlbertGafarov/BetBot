package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.model.NotifyExpiredStatus;

public interface NotifyExpiredBetRepository extends JpaRepository<NotifyExpiredStatus, Long> {
}
