package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.model.Bet;

import java.time.LocalDateTime;
import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query(value = "SELECT * FROM betbot.bets where (initiator_id = ?1 or opponent_id = ?1) and status = 'ACTIVE'", nativeQuery = true)
    List<Bet> getActiveBets(long userId);

    @Query(value = "SELECT * FROM betbot.bets where status = 'ACTIVE' AND finish_date < ?1 AND id NOT IN (SELECT bet_id FROM betbot.notify_expired_status where notify_status = 'NOTIFIED')"
            , nativeQuery = true)
    List<Bet> getExpiredBets(LocalDateTime localDateTime);
}
