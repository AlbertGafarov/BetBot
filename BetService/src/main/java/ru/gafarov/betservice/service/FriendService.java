package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Friend;

public interface FriendService {
    Friend.ResponseFriendInfo getFriendInfo(Friend.Subscribe subscribe);
}
