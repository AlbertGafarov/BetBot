package ru.gafarov.betservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
public class User extends BaseEntity{

    @Column(name = "username")
    private String username;

    @Column(name = "chat_id")
    private long chatId;

}
