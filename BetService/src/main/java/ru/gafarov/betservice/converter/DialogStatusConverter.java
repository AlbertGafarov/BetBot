package ru.gafarov.betservice.converter;

import lombok.experimental.UtilityClass;
import lombok.val;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.entity.DialogStatus;

@UtilityClass
public class DialogStatusConverter {

    public UserOuterClass.DialogStatus toProtoDialogStatus(DialogStatus dialogStatus) {
        if (dialogStatus == null) {
            return null;
        }
        val builder = UserOuterClass.DialogStatus.newBuilder()
                .setId(dialogStatus.getId())
                .setChatStatus(dialogStatus.getChatStatus());
        if (dialogStatus.getBet() != null) {
            builder.setBetId(dialogStatus.getBet().getId());
        }
        return builder.build();
    }
}
