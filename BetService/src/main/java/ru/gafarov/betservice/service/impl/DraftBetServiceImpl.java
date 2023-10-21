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
        draftBet.setInitiator(converter.toUser(protoDraftBet.getInitiator()));
        draftBet.setCreated(LocalDateTime.now());
        draftBet.setUpdated(LocalDateTime.now());
        draftBet.setOpponentName(protoDraftBet.getOpponentName());
        draftBet.setOpponentCode(protoDraftBet.getOpponentCode());
        draftBet.setStatus(Status.ACTIVE);
        draftBet.setWager(protoDraftBet.getWager());
        draftBet = draftBetRepository.save(draftBet);

        return Proto.ResponseMessage.newBuilder().setDraftBet(Proto.DraftBet.newBuilder(protoDraftBet).setId(draftBet.getId()).build()).build();

    }

    @Override
    public Proto.ResponseMessage setOpponentName(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setOpponentName(draftBet.getId(), draftBet.getOpponentName(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseMessage setOpponentCode(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setOpponentCode(draftBet.getId(), draftBet.getOpponentCode(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseMessage setDefinition(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setDefinition(draftBet.getId(), draftBet.getDefinition(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseMessage setWager(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setWager(draftBet.getId(), draftBet.getWager(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseMessage setFinishDate(Proto.DraftBet draftBet) {
        LocalDateTime finishDate = LocalDateTime.now().plusDays(draftBet.getDaysToFinish());
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setFinishDate(draftBet.getId(), finishDate, localDateTime);
        return Proto.ResponseMessage.newBuilder()
                .setDraftBet(converter.toProtoDraftBet(draftBetRepository.findById(draftBet.getId()).get())).build();
    }

    @Override
    public Proto.ResponseMessage getLastDraftBet(Proto.User user) {
        return Proto.ResponseMessage.newBuilder()
                .setDraftBet(converter.toProtoDraftBet(draftBetRepository.getLastDraftBet(user.getId()))).build();
    }

    @Override
    public Proto.ResponseMessage delete(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setStatus(draftBet.getId(), Status.DELETED.toString(), localDateTime);
        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.SUCCESS).build();
    }
}