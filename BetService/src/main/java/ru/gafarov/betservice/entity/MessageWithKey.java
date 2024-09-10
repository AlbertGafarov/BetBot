package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

//@Data
//@Entity
//@Table(name = "message_with_key")
//@NoArgsConstructor
//@EntityListeners(AuditingEntityListener.class)
public class MessageWithKey {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "tg_message_id")
    private Integer tgMessageId; // Номер сообщения в чате, в котором хранится секретный код

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

}