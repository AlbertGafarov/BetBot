package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.model.User;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameIgnoreCase(String name);
    User findByUsernameIgnoreCaseAndCode(String name, int code);
    User findByChatId(long name);

    @Modifying
    @Transactional
    @Query(value = "update users set chat_status = ?2 where chat_id = ?1", nativeQuery = true)
    void changeChatStatus(long chatId, String chatStatus);

    @Query(value = "select u.* from betbot.subscribe s " +
            "join betbot.users u on s.subscribed_id = u.id " +
            "where s.subscriber_id = ?1 and u.chat_id = ?2", nativeQuery = true)
    Optional<User> findFriend(long id, long chatId);

    @Query(value = "select u.* from betbot.subscribe\n" +
            "join betbot.users u on subscriber_id = u.id\n" +
            "where subscribed_id = ?1 \n" +
            "and subscriber_id in (select subscribed_id from betbot.subscribe \n" +
            "where subscriber_id = ?1)", nativeQuery = true)
    List<User> getFriends(long id);
}
