package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.ArgumentConverter;
import ru.gafarov.betservice.entity.Argument;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.BetRole;
import ru.gafarov.betservice.repository.ArgumentRepository;
import ru.gafarov.betservice.service.ArgumentService;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.service.MessageWithKeyService;

@Service
@RequiredArgsConstructor
public class ArgumentServiceImpl implements ArgumentService {

    private final ArgumentRepository argumentRepository;
    private final BetService betService;
    private final MessageWithKeyService messageWithKeyService;

    @Override
    public ProtoBet.ResponseMessage save(ProtoBet.Argument protoArgument) {
        Bet bet = betService.getBet(protoArgument.getAuthor().getId(), protoArgument.getAuthor().getDialogStatus().getBetId());
        Argument argument = ArgumentConverter.toArgument(protoArgument, bet);
        User user = argument.getBetRole().equals(BetRole.INITIATOR) ? argument.getBet().getInitiator() : argument.getBet().getOpponent();
        System.out.println("1 " + user);
        try {

        argument.setText(messageWithKeyService.getSecret(user) + argument.getText());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Argument result = argumentRepository.save(argument);
        if (result.getId() != 0) {
            return betService.showBet(protoArgument.getAuthor().getId(), bet.getId());
        } else {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }
}
