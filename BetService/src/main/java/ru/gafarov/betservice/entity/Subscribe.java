package ru.gafarov.betservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.gafarov.betservice.model.Status;

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
    @Column(name = "secret_key")
    private String secretKey;

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