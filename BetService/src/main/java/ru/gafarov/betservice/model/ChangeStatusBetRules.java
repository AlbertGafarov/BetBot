package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gafarov.bet.grpcInterface.Proto;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "status")
@NoArgsConstructor
public class ChangeStatusBetRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "current_bet_status")
    @Enumerated(EnumType.STRING)
    private Proto.BetStatus currentBetStatus;

    @Column(name = "new_bet_status")
    @Enumerated(EnumType.STRING)
    private Proto.BetStatus newBetStatus;

    @Column(name = "new_rival_bet_status")
    @Enumerated(EnumType.STRING)
    private Proto.BetStatus newRivalBetStatus;

    @Column(name = "message_for_initiator")
    private String messageForInitiator;

    @Column(name = "message_for_opponent")
    private String messageForOpponent;

    @Column(name = "bet_role")
    @Enumerated(EnumType.STRING)
    private BetRole betRole;

    @Column(name = "valid")
    private boolean valid;

    public ChangeStatusBetRules(Proto.BetStatus currentBetStatus, Proto.BetStatus newBetStatus, BetRole betRole) {
        this.currentBetStatus = currentBetStatus;
        this.newBetStatus = newBetStatus;
        this.betRole = betRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeStatusBetRules changeStatusBetRules = (ChangeStatusBetRules) o;
        return currentBetStatus == changeStatusBetRules.currentBetStatus
                && newBetStatus == changeStatusBetRules.newBetStatus
                && betRole.equals(changeStatusBetRules.betRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBetStatus, newBetStatus, betRole);
    }
}
