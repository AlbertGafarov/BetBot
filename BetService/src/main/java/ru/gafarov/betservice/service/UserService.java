package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.User;

public interface UserService {

    Proto.ResponseUser saveUser(Proto.User protoUser);
    Proto.ResponseUser findFriend(Proto.Subscribe subscribe);
    User getUser(Proto.User protoUser);
    Proto.ResponseUser getProtoUser(Proto.User protoUser);
    Proto.ResponseMessage changeChatStatus(Proto.User request);
    Proto.ResponseUser getFriends(Proto.User protoUser);
}
