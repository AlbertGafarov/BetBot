package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.model.DraftBet;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

public interface DraftBetRepository extends JpaRepository<DraftBet, Long> {

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set opponent_name = ?2 where id = ?1", nativeQuery = true)
    void setOpponentName(long id, String opponentName);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set opponent_code = ?2 where id = ?1", nativeQuery = true)
    void setOpponentCode(long id, int opponentCode);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set definition = ?2 where id = ?1", nativeQuery = true)
    void setDefinition(long id, String definition);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set wager = ?2 where id = ?1", nativeQuery = true)
    void setWager(long id, String wager);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set finish_date = ?2 where id = ?1", nativeQuery = true)
    void setFinishDate(long id, LocalDateTime finishDate);
}
