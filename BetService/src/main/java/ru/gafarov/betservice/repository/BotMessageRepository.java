package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.model.BotMessage;

import java.util.List;

public interface BotMessageRepository  extends JpaRepository<BotMessage, Long> {

    @Query(value = "SELECT * FROM betbot.bot_message where status = 'ACTIVE' " +
            "AND user_id = ?1 AND draft_bet_id = ?2 AND message_type = ?3"
            , nativeQuery = true)
    List<BotMessage> getMessage(long id, long id1, String toString);

    @Query(value = "SELECT * FROM betbot.bot_message where status = 'ACTIVE' AND user_id = ?1 AND draft_bet_id = ?2"
            , nativeQuery = true)
    List<BotMessage> getByDraftBet(long userId, long draftBetId);
}
