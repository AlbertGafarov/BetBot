package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.entity.Bet;

import java.time.LocalDateTime;
import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query(value = "SELECT * FROM betbot.bets where (initiator_id = ?1 or opponent_id = ?1) and bet_status = 'ACTIVE'", nativeQuery = true)
    List<Bet> getActiveBets(long userId);

    @Query(value = "SELECT * FROM betbot.bets where (initiator_id = ?1 or opponent_id = ?1) and id = ?2", nativeQuery = true)
    Bet getBet(long userId, long bet_id);

    @Query(value = "SELECT * FROM betbot.bets where bet_status = 'ACTIVE' AND finish_date < ?1 AND id NOT IN (SELECT bet_id FROM betbot.notify_expired_status where notify_status = 'NOTIFIED')"
            , nativeQuery = true)
    List<Bet> getExpiredBets(LocalDateTime localDateTime);
    @Query(value = "SELECT * FROM betbot.bets where (initiator_id = ?1 and opponent_id = ?2 " +
            "or initiator_id = ?2 and opponent_id = ?1) and bet_status = ?3 and status != 'DELETED'", nativeQuery = true)
    List<Bet> getBets(long user_id, long friend_id, String string);
}