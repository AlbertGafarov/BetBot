package ru.gafarov.betservice.model;

import lombok.Data;
import ru.gafarov.bet.grpcInterface.Proto;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "status")
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

    @Column(name = "message")
    private String message;

    public ChangeStatusBetRules(Proto.BetStatus currentBetStatus, Proto.BetStatus newBetStatus) {
        this.currentBetStatus = currentBetStatus;
        this.newBetStatus = newBetStatus;
    }

    public ChangeStatusBetRules() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeStatusBetRules changeStatusBetRules = (ChangeStatusBetRules) o;
        return currentBetStatus == changeStatusBetRules.currentBetStatus && newBetStatus == changeStatusBetRules.newBetStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBetStatus, newBetStatus);
    }
}
