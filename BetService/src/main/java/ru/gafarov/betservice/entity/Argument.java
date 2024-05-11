package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.gafarov.betservice.model.BetRole;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "argument")
@NoArgsConstructor
public class Argument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id")
    private Bet bet; // Спор

    @NonNull
    @Column(name = "text")
    private String text; // Аргумент

    @NonNull
    @Column(name = "bet_role")
    @Enumerated(EnumType.STRING)
    private BetRole betRole; // автор аргумента
}