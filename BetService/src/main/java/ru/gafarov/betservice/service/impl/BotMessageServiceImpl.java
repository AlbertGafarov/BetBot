package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BotMessageOuterClass;
import ru.gafarov.bet.grpcInterface.DrBet;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.entity.BaseEntity;
import ru.gafarov.betservice.entity.BotMessage;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.BotMessageRepository;
import ru.gafarov.betservice.service.BotMessageService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessageServiceImpl implements BotMessageService {

    private final BotMessageRepository botMessageRepository;
    private final Converter converter;

    @Override
    public ProtoBet.ResponseMessage save(BotMessageOuterClass.BotMessage protoBotMessage) {

        BotMessage botMessage = converter.toBotMessage(protoBotMessage);
        botMessage.setStatus(Status.ACTIVE);
        botMessage.setCreated(LocalDateTime.now());
        botMessage.setUpdated(LocalDateTime.now());
        botMessage = botMessageRepository.save(botMessage);
        if (botMessage.getId() > 0) {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.SUCCESS).build();
        } else {
            return ProtoBet.ResponseMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage get(BotMessageOuterClass.BotMessage protoBotMessage) {

        Optional<BotMessage> optionalBotMessage;
        if (protoBotMessage.getTgMessageId() > 0) {
            optionalBotMessage = botMessageRepository.getByTgMessageId(protoBotMessage.getTgMessageId());
        } else {
            optionalBotMessage = botMessageRepository.getMessage(protoBotMessage.getUser().getId()
                            , protoBotMessage.getDraftBet().getId(), protoBotMessage.getType().toString()).stream()
                    .max(Comparator.comparing(BaseEntity::getUpdated));
        }

        return optionalBotMessage.map(botMessage -> BotMessageOuterClass.ResponseBotMessage.newBuilder()
                .setBotMessage(converter.toProtoBotMessage(botMessage))
                .setStatus(Rs.Status.SUCCESS).build()).orElseGet(() -> {
            log.error("Не найдено сообщение: {}", protoBotMessage);
            return BotMessageOuterClass.ResponseBotMessage.newBuilder()
                    .setStatus(Rs.Status.ERROR).build();
        });
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage getAll(DrBet.DraftBet draftBet) {
        List<BotMessageOuterClass.BotMessage> botMessageList = botMessageRepository
                .getByDraftBet(draftBet.getInitiator().getId(), draftBet.getId())
                .stream().map(converter::toProtoBotMessage).collect(Collectors.toList());
        if (botMessageList.isEmpty()) {
            log.error("Не найдено ни одной записи botMessage по draftBet с id: {}", draftBet.getId());
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Rs.Status.ERROR).build();
        }
        return BotMessageOuterClass.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage deleteAll(BotMessageOuterClass.BotMessages botMessages) {
        List<Long> identifications = botMessages.getBotMessageList().stream()
                .map(BotMessageOuterClass.BotMessage::getId).filter(a -> a > 0).collect(Collectors.toList());
        if (!identifications.isEmpty()) {
            try {
                botMessageRepository.markDeleted(identifications);
                return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.SUCCESS).build();
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
            }
        } else {
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        }
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage delete(BotMessageOuterClass.BotMessage botMessage) {

        if (botMessage.getTgMessageId() > 0) {
            botMessageRepository.markDeletedByTgId(botMessage.getTgMessageId());
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.SUCCESS).build();
        } else {
            log.error("tg_message_id: {} невозможен", botMessage.getTgMessageId());
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage getWithout(DrBet.DraftBet draftBet) {
        List<BotMessageOuterClass.BotMessage> botMessageList = botMessageRepository
                .getWithoutDraftBet(draftBet.getInitiator().getId(), draftBet.getId())
                .stream().map(converter::toProtoBotMessage).collect(Collectors.toList());
        if (botMessageList.isEmpty()) {
            log.debug("Не найдено ни одной записи botMessage кроме draftBet с id: {}", draftBet.getId());
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Rs.Status.NOT_FOUND).build();
        }
        return BotMessageOuterClass.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Rs.Status.SUCCESS).build();
    }

    @Override
    public BotMessageOuterClass.ResponseBotMessage getAllByTemplate(BotMessageOuterClass.BotMessage request) {
        List<BotMessage> botMessages = botMessageRepository.getAllByTemplate(
                request.getUser().getId()
                , request.getType().toString()
                , request.getDraftBet().getId()
                , request.getBet().getId()
                , request.getFriend().getId());
        if (!botMessages.isEmpty()) {
            return BotMessageOuterClass.ResponseBotMessage.newBuilder()
                    .setStatus(Rs.Status.SUCCESS)
                    .addAllBotMessages(botMessages.stream().map(converter::toProtoBotMessage).collect(Collectors.toList()))
                    .build();
        } else {
            return BotMessageOuterClass.ResponseBotMessage.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
        }
    }
}
