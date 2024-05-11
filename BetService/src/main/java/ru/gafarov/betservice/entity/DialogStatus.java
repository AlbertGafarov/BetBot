package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.gafarov.bet.grpcInterface.UserOuterClass;

import javax.persistence.*;
import java.time.LocalDateTime;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "dialog_status")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DialogStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_status")
    private UserOuterClass.ChatStatus chatStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id")
    private Bet bet; // Спор

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

}