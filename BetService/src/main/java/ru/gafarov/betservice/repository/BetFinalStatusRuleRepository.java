package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.BetFinalStatusRule;

public interface BetFinalStatusRuleRepository extends JpaRepository<BetFinalStatusRule, Long> {
}
