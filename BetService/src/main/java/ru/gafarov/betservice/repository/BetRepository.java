package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.model.Bet;

public interface BetRepository extends JpaRepository<Bet, Long> {
}
