package ru.gafarov.betservice.service;

import ru.gafarov.bet.grpcInterface.Info;

public interface InfoService {
    Info.ResponseInfo getInfo(ru.gafarov.bet.grpcInterface.Info.RequestInfo request);
}
