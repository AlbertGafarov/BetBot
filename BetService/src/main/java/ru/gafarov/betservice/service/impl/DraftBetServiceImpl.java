package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.DraftBet;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.DraftBetRepository;
import ru.gafarov.betservice.service.DraftBetService;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftBetServiceImpl implements DraftBetService {

    private final DraftBetRepository draftBetRepository;
    private final Converter converter;

    @Override
    public Proto.ResponseDraftBet save(Proto.DraftBet protoDraftBet) {

        DraftBet draftBet = new DraftBet();
        draftBet.setInitiator(converter.toUser(protoDraftBet.getInitiator()));
        draftBet.setCreated(LocalDateTime.now());
        draftBet.setUpdated(LocalDateTime.now());
        draftBet.setOpponentName(protoDraftBet.getOpponentName());
        draftBet.setOpponentCode(protoDraftBet.getOpponentCode());
        draftBet.setStatus(Status.ACTIVE);
        draftBet.setWager(protoDraftBet.getWager());
        draftBet = draftBetRepository.save(draftBet);

        return Proto.ResponseDraftBet.newBuilder().setDraftBet(Proto.DraftBet.newBuilder(protoDraftBet).setId(draftBet.getId()).build()).build();

    }

    @Override
    public Proto.ResponseDraftBet setOpponentName(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setOpponentName(draftBet.getId(), draftBet.getOpponentName(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseDraftBet setOpponentCodeAndName(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            draftBetRepository.setOpponentCodeAndName(draftBet.getId(), draftBet.getOpponentCode(), draftBet.getOpponentName(), localDateTime);
            return Proto.ResponseDraftBet.newBuilder().setStatus(Proto.Status.SUCCESS).build();
        } catch (Exception exception) {
            exception.printStackTrace();
            return Proto.ResponseDraftBet.newBuilder().setStatus(Proto.Status.ERROR).build();
        }
    }

    @Override
    public Proto.ResponseDraftBet setDefinition(Proto.DraftBet protoDraftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (protoDraftBet.getId() == 0) {
            DraftBet draftBet = converter.toDraftBet(protoDraftBet);
            draftBet.setUpdated(localDateTime);
            draftBet.setCreated(localDateTime);
            draftBet.setStatus(Status.ACTIVE);
            draftBet = draftBetRepository.save(draftBet);
            return Proto.ResponseDraftBet.newBuilder().setDraftBet(converter.toProtoDraftBet(draftBet)).build();
        } else {
            draftBetRepository.setDefinition(protoDraftBet.getId(), protoDraftBet.getDefinition(), localDateTime);
            Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
            return optionalDraftBet.map(bet -> Proto.ResponseDraftBet.newBuilder()
                    .setDraftBet(converter.toProtoDraftBet(bet))
                    .setStatus(Proto.Status.SUCCESS).build()).orElseGet(() -> Proto.ResponseDraftBet.newBuilder()
                    .setStatus(Proto.Status.ERROR).build());
        }
    }

    @Override
    public Proto.ResponseDraftBet setWager(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setWager(draftBet.getId(), draftBet.getWager(), localDateTime);
        return null;
    }

    @Override
    public Proto.ResponseDraftBet setFinishDate(Proto.DraftBet protoDraftBet) {
        LocalDateTime finishDate = LocalDateTime.now().plusDays(protoDraftBet.getDaysToFinish());
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setFinishDate(protoDraftBet.getId(), finishDate, localDateTime);

        Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
        if (optionalDraftBet.isPresent()) {
            return Proto.ResponseDraftBet.newBuilder()
                    .setDraftBet(converter.toProtoDraftBet(optionalDraftBet.get())).setStatus(Proto.Status.SUCCESS).build();
        } else {
            log.error("Не найдено ни одного draftBet с id: {}", protoDraftBet.getId());
            return Proto.ResponseDraftBet.newBuilder().setStatus(Proto.Status.ERROR).build();
        }
    }

    @Override
    public Proto.ResponseDraftBet getLastDraftBet(Proto.User user) {
        return Proto.ResponseDraftBet.newBuilder()
                .setDraftBet(converter.toProtoDraftBet(draftBetRepository.getLastDraftBet(user.getId()))).build();
    }

    @Override
    public Proto.ResponseDraftBet delete(Proto.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setStatus(draftBet.getId(), Status.DELETED.toString(), localDateTime);
        return Proto.ResponseDraftBet.newBuilder().setStatus(Proto.Status.SUCCESS).build();
    }

    @Override
    public Proto.ResponseDraftBet getDraftBet(Proto.DraftBet protoDraftBet) {
        Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
        if (optionalDraftBet.isPresent()) {
            DraftBet draftBet = optionalDraftBet.get();
            return Proto.ResponseDraftBet.newBuilder().setDraftBet(converter.toProtoDraftBet(draftBet))
                    .setStatus(Proto.Status.SUCCESS).build();
        } else {
            log.error("Не найдено ни одного draftBet с id: {}", protoDraftBet.getId());
            return Proto.ResponseDraftBet.newBuilder().setStatus(Proto.Status.ERROR).build();
        }
    }
}