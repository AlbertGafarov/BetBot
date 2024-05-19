package ru.gafarov.betservice.converter;

import lombok.experimental.UtilityClass;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.entity.Argument;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.model.Status;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ArgumentConverter {

    public Argument toArgument(ProtoBet.Argument protoArgument, Bet bet) {
        Argument argument = new Argument();
        argument.setBet(bet);
        argument.setText(protoArgument.getText());
        if (protoArgument.getAuthor().getId() == bet.getInitiator().getId()) {
            argument.setBetRole(BetRole.INITIATOR);
        } else if (protoArgument.getAuthor().getId() == bet.getOpponent().getId()) {
            argument.setBetRole(BetRole.OPPONENT);
        }
        argument.setStatus(Status.ACTIVE);
        return argument;
    }

    public List<ProtoBet.Argument> toProtoArguments(List<Argument> arguments) {
        List<ProtoBet.Argument> protoArguments = new ArrayList<>();
        for (Argument argument : arguments) {
            protoArguments.add(toProtoArgument(argument));
        }
        return protoArguments;
    }

    public ProtoBet.Argument toProtoArgument(Argument argument) {
        User author;
        if (BetRole.INITIATOR.equals(argument.getBetRole())) {
            author = argument.getBet().getInitiator();
        } else {
            author = argument.getBet().getOpponent();
        }
        return ProtoBet.Argument.newBuilder()
                .setTimestamp(DateTimeConverter.toTimestamp(argument.getCreated()))
                .setAuthor(UserConverter.toProtoUser(author))
                .setText(argument.getText())
                .build();
    }
}