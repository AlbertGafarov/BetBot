package ru.gafarov.betservice.converter;

import lombok.experimental.UtilityClass;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.User;

@UtilityClass
public class UserConverter {
    public UserOuterClass.User toProtoUser(User user) {
        if (user == null) {
            return null;
        }
        UserOuterClass.User.Builder builder = UserOuterClass.User.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setCode(user.getCode())
                .setChatId(user.getChatId())
                .setEncryptionEnabled(user.isEncryptionEnabled());
        if (user.getDialogStatus()!= null) {
                builder.setDialogStatus(DialogStatusConverter.toProtoDialogStatus(user.getDialogStatus()));
        }
        return builder.build();
    }

    public User toUser(UserOuterClass.User protoUser) {
        User user = new User();
        user.setId(protoUser.getId());
        user.setCode(protoUser.getCode());
        user.setUsername(protoUser.getUsername());
        user.setChatId(protoUser.getChatId());
        user.setEncryptionEnabled(protoUser.getEncryptionEnabled());
        return user;
    }
}
