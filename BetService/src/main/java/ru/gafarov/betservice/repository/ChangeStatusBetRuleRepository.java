package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.entity.ChangeStatusBetRule;

import java.time.LocalDateTime;
import java.util.List;

public interface ChangeStatusBetRuleRepository extends JpaRepository<ChangeStatusBetRule, Long> {

    @Query(value = "SELECT new_bet_status FROM betbot.status where bet_role = ?1 " +
            "and current_bet_status = ?2 " +
            "and valid = true and ((current_timestamp > ?4 OR ?3 = 'LOSE') OR new_bet_status != 'WIN') " +
            "and (current_rival_bet_status = ?3 or current_rival_bet_status is null)", nativeQuery = true)
    List<ProtoBet.UserBetStatus> getNextStatuses(String betRole, String currentBetStatus, String rivalBetStatus, LocalDateTime finishDate);
}
