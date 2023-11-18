package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.BaseEntity;
import ru.gafarov.betservice.model.BotMessage;
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
    public Proto.ResponseMessage save(Proto.BotMessage protoBotMessage) {

        BotMessage botMessage = converter.toBotMessage(protoBotMessage);
        botMessage.setStatus(Status.ACTIVE);
        botMessage.setCreated(LocalDateTime.now());
        botMessage.setUpdated(LocalDateTime.now());
        botMessage = botMessageRepository.save(botMessage);
        if (botMessage.getId() > 0) {
            return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.SUCCESS).build();
        } else {
            return Proto.ResponseMessage.newBuilder().setStatus(Proto.Status.ERROR).build();
        }
    }

    @Override
    public Proto.ResponseBotMessage get(Proto.BotMessage protoBotMessage) {

        Optional<BotMessage> optionalBotMessage;
        if (protoBotMessage.getTgMessageId() > 0) {
            optionalBotMessage = botMessageRepository.getByTgMessageId(protoBotMessage.getTgMessageId());
        } else {
            optionalBotMessage = botMessageRepository.getMessage(protoBotMessage.getUser().getId()
                    , protoBotMessage.getDraftBet().getId(), protoBotMessage.getType().toString()).stream()
                    .max(Comparator.comparing(BaseEntity::getUpdated));
        }

        return optionalBotMessage.map(botMessage -> Proto.ResponseBotMessage.newBuilder()
                .setBotMessage(converter.toProtoBotMessage(botMessage))
                .setStatus(Proto.Status.SUCCESS).build()).orElseGet(() -> {
            log.error("Не найдено сообщение: {}", protoBotMessage);
            return Proto.ResponseBotMessage.newBuilder()
                    .setStatus(Proto.Status.ERROR).build();
        });
    }

    @Override
    public Proto.ResponseBotMessage getAll(Proto.DraftBet draftBet) {
        List<Proto.BotMessage> botMessageList = botMessageRepository
                .getByDraftBet(draftBet.getInitiator().getId(), draftBet.getId())
                .stream().map(converter::toProtoBotMessage).collect(Collectors.toList());
        if (botMessageList.isEmpty()) {
            log.error("Не найдено ни одной записи botMessage по draftBet с id: {}", draftBet.getId());
            return Proto.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Proto.Status.ERROR).build();
        }
        return Proto.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Proto.Status.SUCCESS).build();
    }

    @Override
    public Proto.ResponseBotMessage deleteAll(Proto.BotMessages botMessages) {
        List<Long> identifications = botMessages.getBotMessageList().stream()
                .map(Proto.BotMessage::getId).filter(a -> a > 0).collect(Collectors.toList());
        if (!identifications.isEmpty()) {
            try {
                botMessageRepository.markDeleted(identifications);
                return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.SUCCESS).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.ERROR).build();
            }
        } else {
            return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Proto.ResponseBotMessage delete(Proto.BotMessage botMessage) {

        if (botMessage.getTgMessageId() > 0) {
            botMessageRepository.markDeletedByTgId(botMessage.getTgMessageId());
            return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.SUCCESS).build();
        } else {
            log.error("tg_message_id: {} невозможен", botMessage.getTgMessageId());
            return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.ERROR).build();
        }
    }

    @Override
    public Proto.ResponseBotMessage getWithout(Proto.DraftBet draftBet) {
        List<Proto.BotMessage> botMessageList = botMessageRepository
                .getWithoutDraftBet(draftBet.getInitiator().getId(), draftBet.getId())
                .stream().map(converter::toProtoBotMessage).collect(Collectors.toList());
        if (botMessageList.isEmpty()) {
            log.debug("Не найдено ни одной записи botMessage кроме draftBet с id: {}", draftBet.getId());
            return Proto.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Proto.Status.NOT_FOUND).build();
        }
        return Proto.ResponseBotMessage.newBuilder().addAllBotMessages(botMessageList).setStatus(Proto.Status.SUCCESS).build();
    }

    @Override
    public Proto.ResponseBotMessage getAllByType(Proto.BotMessage request) {
        List<BotMessage> botMessages = botMessageRepository.getAllByType(request.getUser().getId(), request.getType().toString());
        if (!botMessages.isEmpty()) {
            return Proto.ResponseBotMessage.newBuilder()
                    .setStatus(Proto.Status.SUCCESS)
                    .addAllBotMessages(botMessages.stream().map(converter::toProtoBotMessage).collect(Collectors.toList()))
                    .build();
        } else {
            return Proto.ResponseBotMessage.newBuilder().setStatus(Proto.Status.NOT_FOUND).build();
        }
    }
}
