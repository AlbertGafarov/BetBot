package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.gafarov.bet.grpcInterface.Proto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "bets")
@NoArgsConstructor
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

    @NonNull
    @Column(name = "wager")
    private String wager; // Награда, которую получит победитель спора

    @NonNull
    @Column(name = "finish_date")
    private LocalDateTime finishDate; // Дата, после которой будет очевиден результат спора

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_bet_status")
    private Proto.BetStatus initiatorBetStatus; // Статус спора, по мнению инициатора

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "opponent_bet_status")
    private Proto.BetStatus opponentBetStatus; // Статус спора, по мнению оппонента

    @Column(name = "inverse_definition")
    private boolean inverseDefinition;

    @Transient
    private List<Proto.BetStatus> nextOpponentBetStatusList;

    @Transient
    private List<Proto.BetStatus> nextInitiatorBetStatusList;
}
