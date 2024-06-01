package ru.gafarov.betservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@ToString(callSuper = true)
public class User extends BaseEntity {

    @Column(name = "username")
    private String username;

    @Column(name = "code")
    private int code;

    @Column(name = "chat_id")
    private long chatId;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, orphanRemoval = true)
    private DialogStatus dialogStatus;
}
