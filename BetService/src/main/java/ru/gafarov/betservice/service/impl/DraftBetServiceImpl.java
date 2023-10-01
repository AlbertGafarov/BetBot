package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.DraftBet;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.DraftBetRepository;
import ru.gafarov.betservice.service.DraftBetService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DraftBetServiceImpl implements DraftBetService {

    private final DraftBetRepository draftBetRepository;
    private final Converter converter;

    @Override
    public Proto.ResponseMessage save(Proto.DraftBet protoDraftBet) {

        DraftBet draftBet = new DraftBet();
        draftBet.setCreated(LocalDateTime.now());
        draftBet.setOpponentName(protoDraftBet.getOpponentName());
        draftBet.setOpponentCode(protoDraftBet.getOpponentCode());
        draftBet.setStatus(Status.ACTIVE);
        draftBet.setWager(protoDraftBet.getWager());
        draftBet = draftBetRepository.save(draftBet);

        return Proto.ResponseMessage.newBuilder().setDraftBet(Proto.DraftBet.newBuilder(protoDraftBet).setId(draftBet.getId()).build()).build();

    }

    @Override
    public Proto.ResponseMessage setOpponentName(Proto.DraftBet draftBet) {
        draftBetRepository.setOpponentName(draftBet.getId(), draftBet.getOpponentName());
        return null;
    }

    @Override
    public Proto.ResponseMessage setOpponentCode(Proto.DraftBet draftBet) {
        draftBetRepository.setOpponentCode(draftBet.getId(), draftBet.getOpponentCode());
        return null;
    }

    @Override
    public Proto.ResponseMessage setDefinition(Proto.DraftBet draftBet) {
        draftBetRepository.setDefinition(draftBet.getId(), draftBet.getDefinition());
        return null;
    }

    @Override
    public Proto.ResponseMessage setWager(Proto.DraftBet draftBet) {
        draftBetRepository.setWager(draftBet.getId(), draftBet.getWager());
        return null;
    }

    @Override
    public Proto.ResponseMessage setFinishDate(Proto.DraftBet draftBet) {
        LocalDateTime finishDate = LocalDateTime.now().plusDays(draftBet.getDaysToFinish());
        draftBetRepository.setFinishDate(draftBet.getId(), finishDate);
        return Proto.ResponseMessage.newBuilder().setDraftBet(converter.toProtoDraftBet(draftBetRepository.findById(draftBet.getId()).get())).build();
    }
}