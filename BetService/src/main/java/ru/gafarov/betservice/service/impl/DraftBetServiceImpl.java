package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.bet.grpcInterface.UserOuterClass;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.entity.DraftBet;
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
    public DrBet.ResponseDraftBet save(DrBet.DraftBet protoDraftBet) {

        DraftBet draftBet = new DraftBet();
        draftBet.setInitiator(converter.toUser(protoDraftBet.getInitiator()));
        draftBet.setCreated(LocalDateTime.now());
        draftBet.setUpdated(LocalDateTime.now());
        draftBet.setOpponentName(protoDraftBet.getOpponentName());
        draftBet.setOpponentCode(protoDraftBet.getOpponentCode());
        draftBet.setStatus(Status.ACTIVE);
        draftBet.setWager(protoDraftBet.getWager());
        draftBet = draftBetRepository.save(draftBet);

        return DrBet.ResponseDraftBet.newBuilder().setDraftBet(DrBet.DraftBet.newBuilder(protoDraftBet).setId(draftBet.getId()).build()).build();

    }

    @Override
    public DrBet.ResponseDraftBet setOpponentName(DrBet.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setOpponentName(draftBet.getId(), draftBet.getOpponentName(), localDateTime);
        return null;
    }

    @Override
    public DrBet.ResponseDraftBet setOpponentCodeAndName(DrBet.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            draftBetRepository.setOpponentCodeAndName(draftBet.getId(), draftBet.getOpponentCode(), draftBet.getOpponentName(), localDateTime);
            return DrBet.ResponseDraftBet.newBuilder().setStatus(Rs.Status.SUCCESS).build();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return DrBet.ResponseDraftBet.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public DrBet.ResponseDraftBet setDefinition(DrBet.DraftBet protoDraftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (protoDraftBet.getId() == 0) {
            DraftBet draftBet = converter.toDraftBet(protoDraftBet);
            draftBet.setStatus(Status.ACTIVE);
            draftBet = draftBetRepository.save(draftBet);
            return DrBet.ResponseDraftBet.newBuilder().setDraftBet(converter.toProtoDraftBet(draftBet)).build();
        } else {
            draftBetRepository.setDefinition(protoDraftBet.getId(), protoDraftBet.getDefinition(), localDateTime);
            Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
            return optionalDraftBet.map(bet -> DrBet.ResponseDraftBet.newBuilder()
                    .setDraftBet(converter.toProtoDraftBet(bet))
                    .setStatus(Rs.Status.SUCCESS).build()).orElseGet(() -> DrBet.ResponseDraftBet.newBuilder()
                    .setStatus(Rs.Status.ERROR).build());
        }
    }

    @Override
    public DrBet.ResponseDraftBet setWager(DrBet.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setWager(draftBet.getId(), draftBet.getWager(), localDateTime);
        return null;
    }

    @Override
    public DrBet.ResponseDraftBet setFinishDate(DrBet.DraftBet protoDraftBet) {
        LocalDateTime finishDate = LocalDateTime.now().plusDays(protoDraftBet.getDaysToFinish());
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setFinishDate(protoDraftBet.getId(), finishDate, localDateTime);

        Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
        if (optionalDraftBet.isPresent()) {
            return DrBet.ResponseDraftBet.newBuilder()
                    .setDraftBet(converter.toProtoDraftBet(optionalDraftBet.get())).setStatus(Rs.Status.SUCCESS).build();
        } else {
            log.error("Не найдено ни одного draftBet с id: {}", protoDraftBet.getId());
            return DrBet.ResponseDraftBet.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public DrBet.ResponseDraftBet getLastDraftBet(UserOuterClass.User user) {
        return DrBet.ResponseDraftBet.newBuilder()
                .setDraftBet(converter.toProtoDraftBet(draftBetRepository.getLastDraftBet(user.getId()))).build();
    }

    @Override
    public DrBet.ResponseDraftBet delete(DrBet.DraftBet draftBet) {
        LocalDateTime localDateTime = LocalDateTime.now();
        draftBetRepository.setStatus(draftBet.getId(), Status.DELETED.toString(), localDateTime);
        return DrBet.ResponseDraftBet.newBuilder().setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public DrBet.ResponseDraftBet getDraftBet(DrBet.DraftBet protoDraftBet) {
        Optional<DraftBet> optionalDraftBet = draftBetRepository.findById(protoDraftBet.getId());
        if (optionalDraftBet.isPresent()) {
            DraftBet draftBet = optionalDraftBet.get();
            return DrBet.ResponseDraftBet.newBuilder().setDraftBet(converter.toProtoDraftBet(draftBet))
                    .setStatus(Rs.Status.SUCCESS).build();
        } else {
            log.error("Не найдено ни одного draftBet с id: {}", protoDraftBet.getId());
            return DrBet.ResponseDraftBet.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }
}