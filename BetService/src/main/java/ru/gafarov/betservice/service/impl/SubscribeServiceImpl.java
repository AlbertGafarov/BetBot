package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.betservice.model.Bet;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.model.Subscribe;
import ru.gafarov.betservice.repository.SubscribeRepository;
import ru.gafarov.betservice.service.SubscribeService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;
    @Override
    public void checkAndPutForInitiator(Bet bet) {
        List<Subscribe> subscribeList = subscribeRepository
                .findBySubscriberIdAndSubscribedId(bet.getInitiator().getId(), bet.getOpponent().getId());
        if (subscribeList.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Subscribe subscribe = new Subscribe();
            subscribe.setSubscriber(bet.getInitiator());
            subscribe.setSubscribed(bet.getOpponent());
            subscribe.setCreated(localDateTime);
            subscribe.setUpdated(localDateTime);
            subscribe.setStatus(Status.ACTIVE);
            subscribe = subscribeRepository.save(subscribe);
            log.info("Добавлена новая подписка с id: {}", subscribe.getId());
        }
    }

    @Override
    public void checkAndPutForOpponent(Bet bet) {
        List<Subscribe> subscribeList = subscribeRepository
                .findBySubscriberIdAndSubscribedId(bet.getOpponent().getId(), bet.getInitiator().getId());
        if (subscribeList.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Subscribe subscribe = new Subscribe();
            subscribe.setSubscriber(bet.getOpponent());
            subscribe.setSubscribed(bet.getInitiator());
            subscribe.setCreated(localDateTime);
            subscribe.setUpdated(localDateTime);
            subscribe.setStatus(Status.ACTIVE);
            subscribe = subscribeRepository.save(subscribe);
            log.info("Добавлена новая подписка с id: {}", subscribe.getId());
        }
    }
}
