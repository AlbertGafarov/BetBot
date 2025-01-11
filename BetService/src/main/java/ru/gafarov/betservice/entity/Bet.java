package ru.gafarov.betservice.entity;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.gafarov.bet.grpcInterface.ProtoBet;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bets")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bet extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator; // Инициатор спора

    @ManyToOne
    @JoinColumn(name = "opponent_id")
    private User opponent; // Оппонент, которому предложено спорить

    @NonNull
    @Column(name = "definition")
    private String definition; // Утверждение, которое инициатор предлагает оспорить оппоненту, либо согласиться.

    @Column(name = "wager")
    private String wager; // Награда, которую получит победитель спора

    @NonNull
    @Column(name = "finish_date")
    private LocalDateTime finishDate; // Дата, после которой будет очевиден результат спора

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_bet_status")
    private ProtoBet.UserBetStatus initiatorBetStatus; // Статус спора, по мнению инициатора

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "opponent_bet_status")
    private ProtoBet.UserBetStatus opponentBetStatus; // Статус спора, по мнению оппонента

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bet_status")
    private ProtoBet.BetStatus betStatus; // Статус спора

    @Column(name = "inverse_definition")
    private boolean inverseDefinition;

    @OneToMany(mappedBy = "bet", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Argument> arguments = new ArrayList<>();

    @Transient
    private List<ProtoBet.UserBetStatus> nextOpponentBetStatusList;

    @Transient
    private List<ProtoBet.UserBetStatus> nextInitiatorBetStatusList;

    @Column(name = "encrypted")
    private boolean encrypted;
}
