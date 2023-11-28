package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.gafarov.betservice.enums.NotifyStatus;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "notify_expired_status")
@NoArgsConstructor
public class NotifyExpiredStatus extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "bet_id")
    private Bet bet; // Спор

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "notify_status")
    private NotifyStatus notifyStatus; // Статус оповещения

}