package ru.gafarov.betservice.model;

import lombok.*;
import ru.gafarov.bet.grpcInterface.Proto;

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
    private DraftBet draftBet; // пользователь, которому адресовано сообщение

    @Column(name = "tg_message_id")
    private int tgMessageId; // Идентификатор сообщения в Телеграмм

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private Proto.BotMessageType messageType; // Тип сообщения
}
