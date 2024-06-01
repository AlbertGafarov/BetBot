package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.UserConverter;
import ru.gafarov.betservice.model.FriendInfo;
import ru.gafarov.betservice.repository.UserRepository;
import ru.gafarov.betservice.service.FriendService;
import ru.gafarov.betservice.service.UserService;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final UserService userService;

    public Friend.ResponseFriendInfo getFriendInfo(Friend.Subscribe subscribe) {
        Optional<FriendInfo> optional = userRepository.getFriendInfo(subscribe.getSubscriber().getId(), subscribe.getSubscribed().getId());
        if (optional.isPresent()) {
            FriendInfo friendInfo = optional.get();
            return Friend.ResponseFriendInfo.newBuilder()
                    .setFriendInfo(Friend.FriendInfo.newBuilder()
                            .setUser(UserConverter.toProtoUser(userService.getUser(subscribe.getSubscribed())))
                            .setActiveBetCount(friendInfo.getActiveBetCount())
                            .setClosedBetCount(friendInfo.getClosedBetCount())
                            .setWinPercent(friendInfo.getWinPercent())
                            .setStandoffPercent(friendInfo.getStandoffPercent())
                            .setTotalWinPercent(friendInfo.getTotalWinPercent())
                            .setTotalStandoffPercent(friendInfo.getTotalStandoffPercent())
                            .setSubscribed(friendInfo.getSubscribed())
                            .build()).build();
        } else {
            log.error("Получена ошибка при попытке получить инфо о друге с id: {}", subscribe.getSubscribed().getId());
            return Friend.ResponseFriendInfo.newBuilder()
                    .setStatus(Rs.Status.ERROR).build();
        }
    }
}
