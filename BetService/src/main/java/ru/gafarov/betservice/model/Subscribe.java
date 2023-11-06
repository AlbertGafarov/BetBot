package ru.gafarov.betservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@IdClass(Subscribe.SubscribeId.class)
@EntityListeners(AuditingEntityListener.class)
public class Subscribe {

    @Id
    private Long subscriberId;

    @Id
    private Long subscribedId;

    @CreatedDate
    @Column(name = "created")
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscribeId implements Serializable {

        protected Long subscriberId;
        protected Long subscribedId;
    }
}