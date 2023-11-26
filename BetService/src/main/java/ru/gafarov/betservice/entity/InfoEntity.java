package ru.gafarov.betservice.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.gafarov.bet.grpcInterface.Info;
import ru.gafarov.betservice.model.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "info")
@EntityListeners(AuditingEntityListener.class)
public class InfoEntity {

    @Id
    @Enumerated(EnumType.STRING)
    Info.InfoType type;

    private String text;

    @CreatedDate
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    private Status status;
}