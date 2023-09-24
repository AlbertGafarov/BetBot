package ru.gafarov.betservice.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.model.User;
import ru.gafarov.betservice.repository.UserRepository;
import ru.gafarov.betservice.service.UserService;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Proto.ResponseMessage saveUser(Proto.User protoUser) {

        User user = new User();
        user.setUsername(protoUser.getName());
        user.setChatId(protoUser.getChatId());
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        return Proto.ResponseMessage.newBuilder().setUser(Proto.User.newBuilder(protoUser)
                .build()).build();
    }

    @Override
    public User getUser(Proto.User protoUser) {
        return userRepository.findByUsername(protoUser.getName());
    }
}
