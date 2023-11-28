package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Info;
import ru.gafarov.betservice.entity.InfoEntity;
import ru.gafarov.betservice.repository.InfoRepository;
import ru.gafarov.betservice.service.InfoService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final InfoRepository infoRepository;

    @Override
    public Info.ResponseInfo getInfo(Info.RequestInfo request) {
        Optional<InfoEntity> optional = infoRepository.findById(request.getInfoType());
        return optional.map(infoEntity ->
                Info.ResponseInfo.newBuilder().setSuccess(true).setInfoType(request.getInfoType())
                        .setText(infoEntity.getText()).build())
                .orElseGet(() -> {
                    log.error("Не найдено инфо с id: {}", request.getInfoType());
                        return Info.ResponseInfo.newBuilder().setSuccess(false)
                                .setInfoType(request.getInfoType()).build();});
    }
}