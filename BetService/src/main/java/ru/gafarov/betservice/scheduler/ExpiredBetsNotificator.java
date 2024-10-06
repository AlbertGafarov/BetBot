package ru.gafarov.betservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.bet.grpcInterface.ProtoBet;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.NotifyExpiredStatus;
import ru.gafarov.betservice.enums.NotifyStatus;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;
import ru.gafarov.betservice.repository.NotifyExpiredBetRepository;
import ru.gafarov.betservice.service.BetService;
import ru.gafarov.betservice.transformer.BetTransformer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.gafarov.betservice.model.BetRole.INITIATOR;
import static ru.gafarov.betservice.model.BetRole.OPPONENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiredBetsNotificator {

    private final BetService betService;
    private final BetServiceGrpc.BetServiceBlockingStub grpcStub;
    private final ChangeStatusBetRuleRepository ruleRepository;
    private final NotifyExpiredBetRepository notifyExpiredBetRepository;
    private final BetTransformer betTransformer;

    @Scheduled(cron = "0 */10 * ? * *")
    public void checkExpiredBets() {
        log.info("Запущена проверка истекших споров");
        List<Bet> betList = betService.getExpiredBets();
        List<ProtoBet.Bet> protoBetList = betList.stream()
                .map(b -> {
                    b.setNextOpponentBetStatusList(ruleRepository.getNextStatuses(OPPONENT.toString()
                            , b.getOpponentBetStatus().toString()
                            , b.getInitiatorBetStatus().toString()
                            , b.getFinishDate()));
                    b.setNextInitiatorBetStatusList(ruleRepository.getNextStatuses(INITIATOR.toString()
                            , b.getInitiatorBetStatus().toString()
                            , b.getOpponentBetStatus().toString()
                            , b.getFinishDate()));
                    return betTransformer.getDecryptedProtoBet(b.getInitiator().getId(), b);
                }).collect(Collectors.toList());

        log.info("Найдено {}", betList.size());
        if (!betList.isEmpty()) {
            ProtoBet.Bets protoBets = ProtoBet.Bets.newBuilder().addAllBets(protoBetList).build();
            ProtoBet.ResponseMessage response = grpcStub.notifyOfExpiredBets(protoBets);
            if (Rs.Status.SUCCESS.equals(response.getStatus())) {
                for (Bet bet : betList) {
                    NotifyExpiredStatus notify = new NotifyExpiredStatus();
                    notify.setBet(bet);
                    notify.setNotifyStatus(NotifyStatus.NOTIFIED);
                    notify.setCreated(LocalDateTime.now());
                    notify.setUpdated(LocalDateTime.now());
                    notify.setStatus(Status.ACTIVE);
                    notifyExpiredBetRepository.save(notify);
                }
            }
        }
    }
}
