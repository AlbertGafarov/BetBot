package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.model.User;
import ru.gafarov.betservice.repository.UserRepository;
import ru.gafarov.betservice.service.UserService;

import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Converter converter;

    @Override
    public Proto.ResponseMessage saveUser(Proto.User protoUser) {
        int code = 0;
        SortedSet<Integer> codes = userRepository.findByUsername(protoUser.getUsername()).stream().map(User::getCode)
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
        return Proto.ResponseMessage.newBuilder().setUser(Proto.User.newBuilder(protoUser).setCode(code)
                .build()).build();
    }

    @Override
    public Proto.ResponseMessage getProtoUser(Proto.User protoUser) {
        Proto.User respProtoUser;
        // если известен chatId, то ищем по нему
        if (protoUser.getChatId() != 0) {
            respProtoUser = converter.toProtoUser(userRepository.findByChatId(protoUser.getChatId()));
            // в противном случае ищем по имени и коду
        } else {
            respProtoUser = converter.toProtoUser(userRepository.findByUsernameAndCode(protoUser.getUsername(), protoUser.getCode()));
        }
        if (respProtoUser == null) {
            return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.NOT_SUCCESS).build();
        } else {
            return Proto.ResponseMessage.newBuilder().setUser(respProtoUser).setStatus(Proto.Status.SUCCESS).build();
        }
    }

    @Override
    public Proto.ResponseMessage changeChatStatus(Proto.User protoUser) {

            userRepository.changeChatStatus(protoUser.getChatId(), protoUser.getChatStatus().toString());
        return Proto.ResponseMessage.newBuilder().setUser(converter.toProtoUser(getUser(protoUser))).build();
    }

    @Override
    public User getUser(Proto.User protoUser) {
        return userRepository.findByUsernameAndCode(protoUser.getUsername(), protoUser.getCode());
    }
}