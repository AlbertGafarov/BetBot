package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.model.User;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String name);
    User findByUsernameAndCode(String name, int code);
    User findByChatId(long name);

    @Modifying
    @Transactional
    @Query(value = "update users set chat_status = ?2 where chat_id = ?1", nativeQuery = true)
    void changeChatStatus(long chatId, String chatStatus);
}
