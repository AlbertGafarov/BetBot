package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.model.User;
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
    private final Converter converter;

    @Override
    public Proto.ResponseUser saveUser(Proto.User protoUser) {
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
        user.setChatStatus(Proto.ChatStatus.START);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        return Proto.ResponseUser.newBuilder().setUser(Proto.User.newBuilder(protoUser).setCode(code)
                .build()).build();
    }

    @Override
    public Proto.ResponseUser getProtoUser(Proto.User protoUser) {
        Proto.User respProtoUser;
        if(protoUser.getId() != 0) {
            Optional<User> optional = userRepository.findById(protoUser.getId());
            if(optional.isPresent()) {
                return Proto.ResponseUser.newBuilder().setUser(converter.toProtoUser(optional.get()))
                        .setStatus(Proto.Status.SUCCESS).build();
            } else {
                log.error("Не найден пользователь с id: {}", protoUser.getId());
                return Proto.ResponseUser.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
            }
            // если известен chatId, то ищем по нему
        } else if (protoUser.getChatId() != 0) {
            respProtoUser = converter.toProtoUser(userRepository.findByChatId(protoUser.getChatId()));
            if (respProtoUser != null) {
                return Proto.ResponseUser.newBuilder().setUser(respProtoUser).setStatus(Proto.Status.SUCCESS).build();
            } else {
                log.error("Не найден пользователь с chatId: {}", protoUser.getChatId());
                return Proto.ResponseUser.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
            }
            // в противном случае ищем по имени и коду
        } else {
            respProtoUser = converter.toProtoUser(userRepository.findByUsernameIgnoreCaseAndCode(protoUser.getUsername(), protoUser.getCode()));
        }
        if (respProtoUser != null) {
            return Proto.ResponseUser.newBuilder().setUser(respProtoUser).setStatus(Proto.Status.SUCCESS).build();
        } else {
            log.error("Не найден пользователь с username: {} и code: {}", protoUser.getUsername(), protoUser.getCode());
            return Proto.ResponseUser.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Proto.ResponseMessage changeChatStatus(Proto.User protoUser) {

        userRepository.changeChatStatus(protoUser.getChatId(), protoUser.getChatStatus().toString());
        return Proto.ResponseMessage.newBuilder().setUser(converter.toProtoUser(getUser(protoUser)))
                .setStatus(Proto.Status.SUCCESS).build();
    }

    @Override
    public Proto.ResponseUser getFriends(Proto.User protoUser) {
        List<User> friends = userRepository.getFriends(protoUser.getId());
        if (friends.isEmpty()) {
            log.debug("Друзья не найдены");
            return Proto.ResponseUser.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
        }
        return Proto.ResponseUser.newBuilder()
                .addAllUsers(friends.stream().map(converter::toProtoUser).collect(Collectors.toList()))
                .setStatus(Proto.Status.SUCCESS).build();
    }

    @Override
    public Proto.ResponseUser findFriend(Proto.Subscribe subscribe) {
        Optional<User> optional = userRepository.findFriend(subscribe.getSubscriber().getId()
                , subscribe.getSubscribed().getChatId());
        return optional.map(user -> Proto.ResponseUser.newBuilder().setUser(converter.toProtoUser(user))
                .setStatus(Proto.Status.SUCCESS).build()).orElseGet(() -> Proto.ResponseUser.newBuilder()
                .setStatus(Proto.Status.ERROR).build());
    }

    @Override
    public User getUser(Proto.User protoUser) {
        return userRepository.findByUsernameIgnoreCaseAndCode(protoUser.getUsername(), protoUser.getCode());
    }
}