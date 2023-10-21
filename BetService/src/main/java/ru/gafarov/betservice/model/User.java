package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.gafarov.bet.grpcInterface.Proto;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@ToString(callSuper = true)
public class User extends BaseEntity{

    @Column(name = "username")
    private String username;

    @Column(name = "code")
    private int code;

    @Column(name = "chat_id")
    private long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_status")
    private Proto.ChatStatus chatStatus;
}
