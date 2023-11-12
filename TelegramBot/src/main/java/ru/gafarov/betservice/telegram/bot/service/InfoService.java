package ru.gafarov.betservice.telegram.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Info.InfoType;
import ru.gafarov.bet.grpcInterface.Info.RequestInfo;
import ru.gafarov.bet.grpcInterface.Info.ResponseInfo;
import ru.gafarov.bet.grpcInterface.InfoServiceGrpc;
import ru.gafarov.betservice.telegram.bot.components.BetSendMessage;

@Service
@Component
@RequiredArgsConstructor
public class InfoService {

    private final InfoServiceGrpc.InfoServiceBlockingStub grpcInfoStub;

    public BetSendMessage getInfo(InfoType infoType, long chatId) {
        RequestInfo requestInfo = RequestInfo.newBuilder().setInfoType(infoType).build();
        ResponseInfo response = grpcInfoStub.get(requestInfo);
        if (response.getSuccess()) {
            BetSendMessage sendMessage = new BetSendMessage(chatId);
            sendMessage.setText(response.getText());
            return sendMessage;
        }
        return null;
    }
}
