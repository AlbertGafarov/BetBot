package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.User;

public interface UserService {

    UserOuterClass.ResponseUser saveUser(UserOuterClass.User protoUser);

    UserOuterClass.ResponseUser findFriend(Friend.Subscribe subscribe);

    User getUser(UserOuterClass.User protoUser);

    UserOuterClass.ResponseUser getProtoUser(UserOuterClass.User protoUser);

    UserOuterClass.ResponseUser changeChatStatus(UserOuterClass.User request);

    UserOuterClass.ResponseUser getSubscribes(UserOuterClass.User protoUser);

    User getUser(Long id);

    /**
     * Метод изменяет статус шифрования пользователя в БД. У пользователя может быть статусы:
     * шифрование включено: encrypted_enabled = true
     * шифрование выключено: encrypted_enabled = false
     */
    UserOuterClass.ResponseUser changeEncryptedStatus(UserOuterClass.User user);
}
