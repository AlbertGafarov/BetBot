package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Proto;
import ru.gafarov.betservice.converter.Converter;
import ru.gafarov.betservice.model.BotMessage;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.BotMessageRepository;
import ru.gafarov.betservice.service.BotMessageService;

import java.time.LocalDateTime;
import java.util.Optional;

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
        botMessageRepository.save(botMessage);

        return Proto.ResponseMessage.newBuilder().setRequestStatus(Proto.RequestStatus.SUCCESS).build();
    }

    @Override
    public Proto.ResponseBotMessage get(Proto.BotMessage protoBotMessage) {

        BotMessage botMessage = new BotMessage();
        botMessage.setDraftBet(converter.toDraftBet(protoBotMessage.getDraftBet()));
        botMessage.setUser(converter.toUser(protoBotMessage.getUser()));
        botMessage.setMessageType(protoBotMessage.getType());
        System.out.println(botMessage);
        Optional<BotMessage> optionalBotMessage = botMessageRepository.findOne(Example.of(botMessage));
        System.out.println(optionalBotMessage.isPresent());
        return optionalBotMessage.map(message -> Proto.ResponseBotMessage.newBuilder()
                .setBotMessage(converter.toProtoBotMessage(message))
                .setRequestStatus(Proto.RequestStatus.SUCCESS).build()).orElseGet(() -> Proto.ResponseBotMessage.newBuilder()
                .setRequestStatus(Proto.RequestStatus.ERROR).build());
    }
}
