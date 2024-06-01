package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.DialogStatus;

public interface DialogStatusRepository extends JpaRepository<DialogStatus, Long> {
}
