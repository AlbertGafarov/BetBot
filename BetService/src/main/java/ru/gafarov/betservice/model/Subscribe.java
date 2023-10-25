package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "subscribe")
@NoArgsConstructor
public class Subscribe extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private User subscriber; // Подписчик или подписчица

    @ManyToOne
    @JoinColumn(name = "subscribed_id")
    private User subscribed; // Тот или та, на кого подписаны
}
