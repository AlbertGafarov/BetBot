package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.gafarov.betservice.model.BotMessage;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BotMessageRepository extends JpaRepository<BotMessage, Long> {

    @Query(value = "SELECT * FROM bot_message where tg_message_id = ?1", nativeQuery = true)
    Optional<BotMessage> getByTgMessageId(int tgMessageId);

    @Query(value = "SELECT * FROM bot_message where status = 'ACTIVE' " +
            "AND user_id = ?1 AND draft_bet_id = ?2 AND message_type = ?3"
            , nativeQuery = true)
    List<BotMessage> getMessage(long id, long id1, String toString);

    @Query(value = "SELECT * FROM bot_message where status = 'ACTIVE' AND user_id = ?1 AND draft_bet_id = ?2"
            , nativeQuery = true)
    List<BotMessage> getByDraftBet(long userId, long draftBetId);

    @Query(value = "select * from bot_message where user_id = ?1 and message_type = ?2 and status != 'DELETED'"
            , nativeQuery = true)
    List<BotMessage> getAllByType(long id, String type);


    @Modifying
    @Transactional
    @Query(value = "update bot_message set status = 'DELETED' where id in :identifications", nativeQuery = true)
    void markDeleted(@Param("identifications") List<Long> identifications);

    @Modifying
    @Transactional
    @Query(value = "update bot_message set status = 'DELETED' where tg_message_id = ?1", nativeQuery = true)
    void markDeletedByTgId(int tgMessageId);

    @Query(value = "SELECT * FROM bot_message where status = 'ACTIVE' AND user_id = ?1 AND draft_bet_id != ?2 AND draft_bet_id is not null"
            , nativeQuery = true)
    List<BotMessage> getWithoutDraftBet(long userId, long draftBetId);
}
