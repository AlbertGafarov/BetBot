package ru.gafarov.betservice.transformer;

import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.betservice.entity.Bet;

public interface BetTransformer {
    //**/
    ProtoBet.Bet getDecryptedProtoBet(Long userId, Bet bet);

    void setNextStatuses(Bet bet);
}
