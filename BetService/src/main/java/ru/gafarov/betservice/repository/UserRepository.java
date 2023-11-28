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

    @Query(value = "with  \n" +
            //"  --друг подписчик\n" +
            "  subscribed as (\n" +
            "    select case when count(*) > 0 then true else false end as subscribed\n" +
            "    from betbot.subscribe where status = 'ACTIVE'\n" +
            "      and subscriber_id = ?1\n" +
            "      and subscribed_id = ?2),\n" +
            //"  --Всего побед\n" +
            "  win_total_cnt as (\n" +
            "      select CAST(count(*) as real) as win_total_cnt\n" +
            "    from betbot.bets\n" +
            "    where (initiator_id = ?2 and initiator_bet_status in ('WIN','WAGERRECIEVED') and opponent_bet_status in ('LOSE','WAGERPAID')\n" +
            "              or opponent_id = ?2 and opponent_bet_status in ('WIN','WAGERRECIEVED') and initiator_bet_status in ('LOSE','WAGERPAID'))\n" +
            "      and status != 'DELETED'),\n" +
            //"  --Всего оконченных споров, может и неоплаченных\n" +
            "  not_cancel_bets as (\n" +
            "      select CAST(count(*) as real) as not_cancel_bets\n" +
            "    from betbot.bets where bet_status IN ('CLOSED','WAIT_WAGER_PAY')\n" +
            "          and (initiator_id = ?2 or opponent_id = ?2)\n" +
            "      and status != 'DELETED'),\n" +
            //"  --Всего ничьих\n" +
            "    standoff_total_cnt as (\n" +
            "    select CAST(count(*) as real) as standoff_total_cnt\n" +
            "    from betbot.bets\n" +
            "      where (initiator_id = ?2 and initiator_bet_status = 'STANDOFF' and opponent_bet_status in ('LOSE','STANDOFF')\n" +
            "         or opponent_id = ?2 and opponent_bet_status = 'STANDOFF' and initiator_bet_status  in ('LOSE','STANDOFF'))\n" +
            "       and status != 'DELETED'),\n" +
            //"  --Наших оконченных споров, может и неоплаченных\n" +
            "  ourNotCancelBets as (\n" +
            "    select CAST(count(*) as real) as our_not_cancel_bets\n" +
            "    from betbot.bets\n" +
            "      where bet_status IN ('CLOSED','WAIT_WAGER_PAY') and (initiator_id = ?2 and opponent_id = ?1\n" +
            "        or opponent_id = ?2 and initiator_id = ?1)\n" +
            "      and status != 'DELETED'),\n" +
            //"  --Наших закрытых споров\n" +
            "  closedBetCount as (\n" +
            "    select count(*) as closed_bet_count\n" +
            "    from betbot.bets\n" +
            "    where (initiator_id = ?2 and opponent_id = ?1\n" +
            "          or initiator_id = ?1 and opponent_id = ?2)\n" +
            "    and bet_status = 'CLOSED' and status != 'DELETED'),\n" +
            //"  -- Наших активных споров\n" +
            "  activeBetCount as (\n" +
            "    select count(*) as active_bet_count\n" +
            "    from betbot.bets where (initiator_id = ?2 and opponent_id = ?1\n" +
            "            or initiator_id = ?1 and opponent_id = ?2)\n" +
            "      and bet_status = 'ACTIVE' and status != 'DELETED'),\n" +
            //"  -- Всего побед среди наших\n" +
            "  win_cnt as (\n" +
            "    select CAST(count(*) as real) as win_cnt from betbot.bets\n" +
            "      where (initiator_id = ?2 and opponent_id = ?1 and initiator_bet_status in ('WIN','WAGERRECIEVED') and opponent_bet_status in ('LOSE','WAGERPAID')\n" +
            "              or opponent_id = ?2 and initiator_id = ?1 and opponent_bet_status in ('WIN','WAGERRECIEVED') and initiator_bet_status in ('LOSE','WAGERPAID'))\n" +
            "      and status != 'DELETED'),\n" +
            //"  --Всего наших ничьих\n" +
            "    standoff_cnt as (\n" +
            "      select CAST(count(*) as real) as standoff_cnt\n" +
            "    from betbot.bets\n" +
            "    where (initiator_id = ?2 and opponent_id = ?1 and initiator_bet_status = 'STANDOFF' and opponent_bet_status in ('LOSE','STANDOFF')\n" +
            "            or opponent_id = ?2 and initiator_id = ?1 and opponent_bet_status = 'STANDOFF' and initiator_bet_status  in ('LOSE','STANDOFF'))\n" +
            "    and status != 'DELETED')\n" +
            "select\n" +
            "  subscribed,\n" +
            "  CASE WHEN not_cancel_bets > 0 THEN CAST(win_total_cnt*100/not_cancel_bets as real) ELSE 0 END as totalwinpercent,\n" +
            "    CASE WHEN not_cancel_bets > 0 THEN CAST(standoff_total_cnt*100/not_cancel_bets as real) ELSE 0 END as totalstandoffpercent,\n" +
            "    CASE WHEN our_not_cancel_bets > 0 THEN CAST(win_cnt*100/our_not_cancel_bets as real) ELSE 0 END as winpercent,\n" +
            "    CASE WHEN our_not_cancel_bets > 0 THEN CAST(standoff_cnt*100/our_not_cancel_bets as real) ELSE 0 END as standoffpercent,\n" +
            "    CAST(closedbetcount.closed_bet_count as integer) as closedBetCount,\n" +
            "    CAST(activebetcount.active_bet_count as integer) as activeBetCount\n" +
            "from win_total_cnt, win_cnt, not_cancel_bets, standoff_total_cnt, closedBetCount, activeBetCount, subscribed, standoff_cnt, ourNotCancelBets"
            , nativeQuery = true)
    Optional<FriendInfo> getFriendInfo(long user_id, long friend_id);
}