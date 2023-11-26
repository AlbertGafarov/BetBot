package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.model.BetRole;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "status")
@NoArgsConstructor
public class ChangeStatusBetRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "current_bet_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus currentBetStatus;

    @Column(name = "new_bet_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus newBetStatus;

    @Column(name = "new_rival_bet_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus newRivalBetStatus;

    @Column(name = "message_for_initiator")
    private String messageForInitiator;

    @Column(name = "message_for_opponent")
    private String messageForOpponent;

    @Column(name = "bet_role")
    @Enumerated(EnumType.STRING)
    private BetRole betRole;

    @Column(name = "valid")
    private boolean valid;

    public ChangeStatusBetRule(ProtoBet.UserBetStatus currentBetStatus, ProtoBet.UserBetStatus newBetStatus, BetRole betRole) {
        this.currentBetStatus = currentBetStatus;
        this.newBetStatus = newBetStatus;
        this.betRole = betRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeStatusBetRule changeStatusBetRule = (ChangeStatusBetRule) o;
        return currentBetStatus == changeStatusBetRule.currentBetStatus
                && newBetStatus == changeStatusBetRule.newBetStatus
                && betRole.equals(changeStatusBetRule.betRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBetStatus, newBetStatus, betRole);
    }
}
