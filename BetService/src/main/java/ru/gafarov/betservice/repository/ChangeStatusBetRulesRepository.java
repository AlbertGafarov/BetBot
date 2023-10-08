package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.ChangeStatusBetRules;

import java.util.List;

public interface ChangeStatusBetRulesRepository extends JpaRepository<ChangeStatusBetRules, Long> {

    @Query(value = "SELECT new_bet_status FROM betbot.status where bet_role = ?1 \n" +
            "and current_bet_status = ?2\n" +
            "and valid = true", nativeQuery = true)
    List<Proto.BetStatus> getNextStatuses(String betRole, String currentBetStatus);
}
