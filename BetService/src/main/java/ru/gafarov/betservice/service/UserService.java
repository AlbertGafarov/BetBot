package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.User;

public interface UserService {

    Proto.ResponseMessage saveUser(Proto.User protoUser);
    User getUser(Proto.User protoUser);
    Proto.ResponseMessage getProtoUser(Proto.User protoUser);
    Proto.ResponseMessage changeChatStatus(Proto.User request);
}
