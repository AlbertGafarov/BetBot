package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "draft_bets")
@NoArgsConstructor
public class DraftBet extends BaseEntity {

    @Column(name = "opponent_name")
    private String opponentName; // Оппонент, которому предложено спорить

    @Column(name = "opponent_code")
    private int opponentCode; // код оппонента

    @Column(name = "definition")
    private String definition; // Утверждение, которое инициатор предлагает оспорить оппоненту, либо согласиться.

    @Column(name = "wager")
    private String wager; // Награда, которую получит победитель спора

    @Column(name = "finish_date")
    private LocalDateTime finishDate; // Дата, после которой будет очевиден результат спора
}
