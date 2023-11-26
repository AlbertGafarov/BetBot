package ru.gafarov.betservice.entity;

import lombok.*;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "bot_message")
@ToString(callSuper = true)
@NoArgsConstructor
public class BotMessage extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // пользователь, которому адресовано сообщение

    @ManyToOne
    @JoinColumn(name = "draft_bet_id")
    private DraftBet draftBet;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    @ManyToOne
    @JoinColumn(name = "bet_id")
    private Bet bet;

    @Column(name = "tg_message_id")
    private int tgMessageId; // Идентификатор сообщения в Телеграмм

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private BotMessageOuterClass.BotMessageType messageType; // Тип сообщения
}
