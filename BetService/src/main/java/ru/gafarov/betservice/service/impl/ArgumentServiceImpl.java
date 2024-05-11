package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.ArgumentConverter;
import ru.gafarov.betservice.entity.Argument;
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

        Argument argument = argumentRepository.save(ArgumentConverter.toArgument(protoArgument));
        if (argument.getId() != 0) {
            return betService.showBet(protoArgument.getBet());
        } else {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }
}
