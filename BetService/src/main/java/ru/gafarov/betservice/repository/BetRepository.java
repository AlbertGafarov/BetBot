package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.Bet;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query(value = "SELECT * FROM betbot.bets where (initiator_id = ?1 or opponent_id = ?1) and status = 'ACTIVE'", nativeQuery = true)
    List<Bet> getActiveBets(long userId);
}
