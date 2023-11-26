package ru.gafarov.betservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gafarov.bet.grpcInterface.ProtoBet;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "bet_status_rule")
@IdClass(BetStatusRule.BetStatusRuleId.class)
@NoArgsConstructor
public class BetStatusRule {

    @Id
    @Column(name = "initiator_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus initiatorBetStatus;

    @Id
    @Column(name = "opponent_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.UserBetStatus opponentBetStatus;

    @Column(name = "bet_status")
    @Enumerated(EnumType.STRING)
    private ProtoBet.BetStatus betStatus;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BetStatusRuleId implements Serializable {
        private ProtoBet.UserBetStatus initiatorBetStatus;
        private ProtoBet.UserBetStatus opponentBetStatus;
    }

}