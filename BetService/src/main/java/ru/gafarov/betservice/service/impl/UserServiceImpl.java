package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.converter.UserConverter;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.DialogStatus;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.DialogStatusRepository;
import ru.gafarov.betservice.repository.UserRepository;
import ru.gafarov.betservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DialogStatusRepository dialogStatusRepository;

    @Override
    public UserOuterClass.ResponseUser saveUser(UserOuterClass.User protoUser) {
        int code = 0;
        SortedSet<Integer> codes = userRepository.findByUsernameIgnoreCase(protoUser.getUsername()).stream().map(User::getCode)
                .collect(Collectors.toCollection(TreeSet::new));
        while (codes.contains(code) || code == 0) {
            code = (int) Math.round(Math.random() * 10000);
        }

        User user = new User();
        user.setUsername(protoUser.getUsername());
        user.setCode(code);
        user.setChatId(protoUser.getChatId());
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        user.setStatus(Status.ACTIVE);

        DialogStatus dialogStatus = new DialogStatus();
        dialogStatus.setChatStatus(UserOuterClass.ChatStatus.START);
        user = userRepository.save(user);
        dialogStatus.setUser(user);
        dialogStatusRepository.save(dialogStatus);
        return UserOuterClass.ResponseUser.newBuilder().setUser(UserOuterClass.User.newBuilder(protoUser).setCode(code)
                .setId(user.getId())
                .build()).build();
    }

    @Override
    public UserOuterClass.ResponseUser getProtoUser(UserOuterClass.User protoUser) {
        UserOuterClass.User respProtoUser;
        if (protoUser.getId() != 0) {
            Optional<User> optional = userRepository.findById(protoUser.getId());
            if (optional.isPresent()) {
                return UserOuterClass.ResponseUser.newBuilder().setUser(UserConverter.toProtoUser(optional.get()))
                        .setStatus(Rs.Status.SUCCESS).build();
            } else {
                log.error("Не найден пользователь с id: {}", protoUser.getId());
                return UserOuterClass.ResponseUser.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
            }
            // если известен chatId, то ищем по нему
        } else if (protoUser.getChatId() != 0) {
            respProtoUser = UserConverter.toProtoUser(userRepository.findByChatId(protoUser.getChatId()));
            if (respProtoUser != null) {
                return UserOuterClass.ResponseUser.newBuilder().setUser(respProtoUser).setStatus(Rs.Status.SUCCESS).build();
            } else {
                log.error("Не найден пользователь с chatId: {}", protoUser.getChatId());
                return UserOuterClass.ResponseUser.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
            }
            // в противном случае ищем по имени и коду
        } else {
            respProtoUser = UserConverter.toProtoUser(userRepository.findByUsernameIgnoreCaseAndCode(protoUser.getUsername(), protoUser.getCode()));
        }
        if (respProtoUser != null) {
            return UserOuterClass.ResponseUser.newBuilder().setUser(respProtoUser).setStatus(Rs.Status.SUCCESS).build();
        } else {
            log.error("Не найден пользователь с username: {} и code: {}", protoUser.getUsername(), protoUser.getCode());
            return UserOuterClass.ResponseUser.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        }
    }

    @Override
    public UserOuterClass.ResponseUser changeChatStatus(UserOuterClass.User protoUser) {

        DialogStatus dialogStatus = new DialogStatus();
        dialogStatus.setId(protoUser.getDialogStatus().getId());
        dialogStatus.setChatStatus(protoUser.getDialogStatus().getChatStatus());
        dialogStatus.setUser(UserConverter.toUser(protoUser));
        if (protoUser.getDialogStatus().getBetId() > 0) {
            Bet bet = new Bet();
            bet.setId(protoUser.getDialogStatus().getBetId());
            dialogStatus.setBet(bet);
        }
        dialogStatusRepository.save(dialogStatus);
        return UserOuterClass.ResponseUser.newBuilder().setUser(UserConverter.toProtoUser(getUser(protoUser)))
                .setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public UserOuterClass.ResponseUser getSubscribes(UserOuterClass.User protoUser) {
        List<User> friends = userRepository.getSubscribes(protoUser.getId());
        if (friends.isEmpty()) {
            log.debug("Друзья не найдены");
            return UserOuterClass.ResponseUser.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        }
        return UserOuterClass.ResponseUser.newBuilder()
                .addAllUsers(friends.stream().map(UserConverter::toProtoUser).collect(Collectors.toList()))
                .setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public UserOuterClass.ResponseUser findFriend(Friend.Subscribe subscribe) {
        Optional<User> optional = userRepository.findFriend(subscribe.getSubscriber().getId()
                , subscribe.getSubscribed().getId()
                , subscribe.getSubscribed().getChatId());
        return optional.map(user -> UserOuterClass.ResponseUser.newBuilder().setUser(UserConverter.toProtoUser(user))
                .setStatus(Rs.Status.SUCCESS).build()).orElseGet(() -> UserOuterClass.ResponseUser.newBuilder()
                .setStatus(Rs.Status.NOT_FOUND).build());
    }

    @Override
    public User getUser(UserOuterClass.User protoUser) {
        User user = null;
        if (protoUser.getId() != 0) {
            Optional<User> optional = userRepository.findById(protoUser.getId());
            if (optional.isEmpty()) {
                log.error("Не найден пользователь с id: {}", protoUser.getId());
            } else {
                user = optional.get();
            }
            // если известен chatId, то ищем по нему
        } else if (protoUser.getChatId() != 0) {
            user = userRepository.findByChatId(protoUser.getChatId());
            if (user == null) {
                log.error("Не найден пользователь с chatId: {}", protoUser.getChatId());

                // в противном случае ищем по имени и коду
            } else {
                user = userRepository.findByUsernameIgnoreCaseAndCode(protoUser.getUsername(), protoUser.getCode());
                if (user == null) {
                    log.error("Не найден пользователь с username: {} и code: {}", protoUser.getUsername(), protoUser.getCode());
                }
            }
        }
        return user;
    }
}