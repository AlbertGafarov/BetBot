package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.model.ChangeStatusBetRules;

public interface ChangeStatusBetRulesRepository extends JpaRepository<ChangeStatusBetRules, Long> {
}
