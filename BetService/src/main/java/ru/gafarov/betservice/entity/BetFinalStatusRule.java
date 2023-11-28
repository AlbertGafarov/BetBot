package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.model.Status;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "bet_final_status_rule")
@NoArgsConstructor
public class BetFinalStatusRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "initiator_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus initiatorBetStatus;

    @Column(name = "opponent_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus opponentBetStatus;

    @Column(name = "bet_final_status")
    @Enumerated(EnumType.STRING)
    private Status betFinalStatus;

    public BetFinalStatusRule(ProtoBet.UserBetStatus initiatorBetStatus, ProtoBet.UserBetStatus opponentBetStatus, Status betFinalStatus) {
        this.initiatorBetStatus = initiatorBetStatus;
        this.opponentBetStatus = opponentBetStatus;
        this.betFinalStatus = betFinalStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetFinalStatusRule that = (BetFinalStatusRule) o;
        return initiatorBetStatus == that.initiatorBetStatus && opponentBetStatus == that.opponentBetStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiatorBetStatus, opponentBetStatus);
    }
}