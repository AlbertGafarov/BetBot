package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.ArgumentConverter;
import ru.gafarov.betservice.entity.Argument;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.repository.ArgumentRepository;
import ru.gafarov.betservice.service.ArgumentService;
import ru.gafarov.betservice.service.BetService;

@Service
@RequiredArgsConstructor
public class ArgumentServiceImpl implements ArgumentService {

    private final ArgumentRepository argumentRepository;
    private final BetService betService;

    @Override
    public ProtoBet.ResponseMessage save(ProtoBet.Argument protoArgument) {
        Bet bet = betService.getBet(protoArgument.getAuthor().getId(), protoArgument.getAuthor().getDialogStatus().getBetId());

        Argument argument = argumentRepository.save(ArgumentConverter.toArgument(protoArgument, bet));
        if (argument.getId() != 0) {
            return betService.showBet(protoArgument.getAuthor().getId(), bet.getId());
        } else {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }
}
