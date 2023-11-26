package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.BetStatusRule;

public interface BetStatusRuleRepository extends JpaRepository<BetStatusRule, BetStatusRule.BetStatusRuleId> {
}
