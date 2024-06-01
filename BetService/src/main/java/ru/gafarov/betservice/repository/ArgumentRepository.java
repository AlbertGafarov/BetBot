package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.entity.Argument;

public interface ArgumentRepository extends JpaRepository<Argument, Long> {
}
