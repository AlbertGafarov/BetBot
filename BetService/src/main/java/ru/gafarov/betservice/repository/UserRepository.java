package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.betservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String name);
}
