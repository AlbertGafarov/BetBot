package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.entity.DraftBet;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

public interface DraftBetRepository extends JpaRepository<DraftBet, Long> {

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set opponent_name = ?2, updated = ?3 where id = ?1", nativeQuery = true)
    void setOpponentName(long id, String opponentName, LocalDateTime localDateTime);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set opponent_code = ?2, opponent_name = ?3, updated = ?4 where id = ?1", nativeQuery = true)
    void setOpponentCodeAndName(long id, int opponentCode, String opponentName, LocalDateTime localDateTime);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set definition = ?2, updated = ?3 where id = ?1", nativeQuery = true)
    void setDefinition(long id, String definition, LocalDateTime localDateTime);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set wager = ?2, updated = ?3 where id = ?1", nativeQuery = true)
    void setWager(long id, String wager, LocalDateTime localDateTime);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set finish_date = ?2, updated = ?3 where id = ?1", nativeQuery = true)
    void setFinishDate(long id, LocalDateTime finishDate, LocalDateTime localDateTime);

    @Modifying
    @Transactional
    @Query(value = "update draft_bets set status = ?2 where id = ?1", nativeQuery = true)
    void setStatus(long id, String status, LocalDateTime localDateTime);

    @Query(value = "SELECT * FROM draft_bets where initiator_id = ?1 and status = 'ACTIVE' order by updated desc limit 1", nativeQuery = true)
    DraftBet getLastDraftBet(long userId);

}
