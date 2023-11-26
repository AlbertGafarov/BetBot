package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.FriendInfo;

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

    @Query(value = "select u.* from subscribe s " +
            "join users u on s.subscribed_id = u.id " +
            "where ?2 in (subscribed_id, 0) " +
            "and ?1 = subscriber_id " +
            "and ?3 in (chat_id, 0) " +
            "and s.status = 'ACTIVE' " +
            "and (select count(*) from subscribe " +
                "where status = 'ACTIVE' " +
                "and ?2 in (subscriber_id, 0) " +
                "and ?3 in (chat_id, 0) " +
                "and ?1 = subscribed_id) > 0", nativeQuery = true)
    Optional<User> findFriend(long id, long friend_id, long chatId);

    @Query(value = "select u.* from subscribe s " +
            "join users u on s.subscribed_id = u.id " +
            "where s.subscriber_id = ?1 " +
            "and s.status = 'ACTIVE'", nativeQuery = true)
    List<User> getSubscribes(long id);

    @Query(
            value = "with " +
            //друг подписчик
            "subscribed as ( " +
            "select case when count(*) > 0 then true else false end as subscribed from subscribe where status = 'ACTIVE' " +
            "and subscriber_id = ?1 " +
            "and subscribed_id = ?2), " +

            //Всего побед
            "win_total_cnt as ( " +
            "select cast(count(*) as real) as win_total_cnt from bets " +
            "where initiator_id = ?2 and initiator_bet_status in ('WIN','WAGERRECIEVED') " +
            "and opponent_bet_status in ('LOSE','WAGERPAID') " +
            "or opponent_id = ?2 and opponent_bet_status in ('WIN','WAGERRECIEVED') and initiator_bet_status  in ('LOSE','WAGERPAID') " +
            "and status != 'DELETED'), " +

            //Всего неотмененных споров
            "not_cancel_bets as ( " +
            "select count(*) as not_cancel_bets from bets where bet_status != 'CANCEL' " +
            "and initiator_id = ?2 or opponent_id = ?2 " +
            "and status != 'DELETED'), " +

            //Всего ничьих
            "standoff_total_cnt as ( " +
            "select cast(count(*) as real) as standoff_total_cnt from bets where initiator_id = ?2 and initiator_bet_status = 'STANDOFF' " +
            "and opponent_bet_status in ('LOSE','STANDOFF') " +
            "or opponent_id = ?2 and opponent_bet_status = 'STANDOFF' and initiator_bet_status  in ('LOSE','STANDOFF') " +
            "and status != 'DELETED'), " +

            //Наших закрытых споров
            "closedBetCount as ( " +
            "select count(*) as closed_bet_count from bets where (initiator_id = ?2 and opponent_id = ?1 " +
            "or initiator_id = ?1 and opponent_id = ?2) and bet_status = 'CLOSED'" +
            "and status != 'DELETED'), " +

            // Наших активных споров
            "activeBetCount as ( " +
            "select count(*) as active_bet_count from bets where (initiator_id = ?2 and opponent_id = ?1 " +
            "or initiator_id = ?1 and opponent_id = ?2) and bet_status = 'ACTIVE' " +
            "and status != 'DELETED'), " +

            // Всего побед среди наших
            "win_cnt as ( " +
            "select  cast(count(*) as real) as win_cnt from bets where initiator_id = ?2 and opponent_id = ?1 " +
            "and initiator_bet_status in ('WIN','WAGERRECIEVED') and opponent_bet_status in ('LOSE','WAGERPAID') " +
            "or opponent_id = ?2 and initiator_id = ?1 and opponent_bet_status in ('WIN','WAGERRECIEVED') " +
            "and initiator_bet_status  in ('LOSE','WAGERPAID') " +
            "and status != 'DELETED'), " +

            //Всего наших ничьих
            "standoff_cnt as ( " +
            "select cast(count(*) as real) as standoff_cnt from bets where initiator_id = ?2 and opponent_id = ?1 " +
            "and initiator_bet_status = 'STANDOFF' and opponent_bet_status in ('LOSE','STANDOFF') " +
            "or opponent_id = ?2 and initiator_id = ?1 and opponent_bet_status = 'STANDOFF' " +
            "and initiator_bet_status  in ('LOSE','STANDOFF') " +
            "and status != 'DELETED') " +

            "select " +
            "subscribed, " +
            "CAST(win_total_cnt*100/not_cancel_bets as real) as totalwinpercent, " +
            "CAST(standoff_total_cnt*100/not_cancel_bets as real) as totalstandoffpercent, " +
            "CAST(win_cnt*100/closed_bet_count as real) as winpercent, " +
            "CAST(standoff_cnt*100/closed_bet_count as real)  as standoffpercent, " +
            "CAST(closedbetcount.closed_bet_count as integer) as closedBetCount, " +
            "CAST(activebetcount.active_bet_count as integer) as activeBetCount " +
            "from win_total_cnt, win_cnt, not_cancel_bets, standoff_total_cnt, closedBetCount, activeBetCount, subscribed, standoff_cnt"
            , nativeQuery = true)
    Optional<FriendInfo> getFriendInfo(long user_id, long friend_id);
}