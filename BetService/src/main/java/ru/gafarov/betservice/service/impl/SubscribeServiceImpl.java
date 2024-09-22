package ru.gafarov.betservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gafarov.bet.grpcInterface.Friend;
import ru.gafarov.bet.grpcInterface.Rs;
import ru.gafarov.betservice.entity.Bet;
import ru.gafarov.betservice.entity.Subscribe;
import ru.gafarov.betservice.entity.User;
import ru.gafarov.betservice.model.Status;
import ru.gafarov.betservice.repository.SubscribeRepository;
import ru.gafarov.betservice.service.SubscribeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;

    @Override
    public void checkAndPutForInitiator(Bet bet) {
        Optional<Subscribe> optional = subscribeRepository
                .findBySubscriberIdAndSubscribedId(bet.getInitiator().getId(), bet.getOpponent().getId());
        if (optional.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Subscribe subscribe = new Subscribe();
            subscribe.setSubscriberId(bet.getInitiator().getId());
            subscribe.setSubscribedId(bet.getOpponent().getId());
            subscribe.setCreated(localDateTime);
            subscribe.setUpdated(localDateTime);
            subscribe.setStatus(Status.ACTIVE);
            subscribe = subscribeRepository.save(subscribe);
            log.info("Добавлена новая подписка {} : {}", subscribe.getSubscriberId(), subscribe.getSubscribedId());
        }
    }

    @Override
    public void checkAndPutForOpponent(Bet bet) {
        Optional<Subscribe> optional = subscribeRepository
                .findBySubscriberIdAndSubscribedId(bet.getOpponent().getId(), bet.getInitiator().getId());
        if (optional.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Subscribe subscribe = new Subscribe();
            subscribe.setSubscriberId(bet.getOpponent().getId());
            subscribe.setSubscribedId(bet.getInitiator().getId());
            subscribe.setCreated(localDateTime);
            subscribe.setUpdated(localDateTime);
            subscribe.setStatus(Status.ACTIVE);
            subscribe = subscribeRepository.save(subscribe);
            log.info("Добавлена новая подписка {} : {}", subscribe.getSubscriberId(), subscribe.getSubscribedId());
        }
    }

    @Override
    public Rs.Response addSubscribe(Friend.Subscribe protoSubscribe) {
        Subscribe subscribe;
        try {
            Optional<Subscribe> optional = subscribeRepository.findBySubscriberIdAndSubscribedId(protoSubscribe.getSubscriber().getId()
                    , protoSubscribe.getSubscribed().getId());
            if (optional.isPresent()) {
                subscribe = optional.get();
                if (subscribe.getStatus().equals(Status.ACTIVE)) {
                    return Rs.Response.newBuilder().setStatus(Rs.Status.REDUNDANT).build();
                }
            } else {
                subscribe = new Subscribe();
                subscribe.setSubscriberId(protoSubscribe.getSubscriber().getId());
                subscribe.setSubscribedId(protoSubscribe.getSubscribed().getId());
            }
            subscribe.setStatus(Status.ACTIVE);

            subscribeRepository.save(subscribe);
            return Rs.Response.newBuilder().setStatus(Rs.Status.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Rs.Response.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public Rs.Response delete(Friend.Subscribe protoSubscribe) {
        try {
            Optional<Subscribe> optional = subscribeRepository.findBySubscriberIdAndSubscribedId(protoSubscribe.getSubscriber().getId()
            , protoSubscribe.getSubscribed().getId());
            if (optional.isPresent()) {
                Subscribe subscribe = optional.get();
                subscribe.setStatus(Status.DELETED);
                subscribeRepository.save(subscribe);
            return Rs.Response.newBuilder().setStatus(Rs.Status.SUCCESS).build();
            } else {
                return Rs.Response.newBuilder().setStatus(Rs.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Rs.Response.newBuilder().setStatus(Rs.Status.ERROR).build();
        }
    }

    @Override
    public Subscribe getSubscribe(User subscriber, User subscribed) {
        Optional<Subscribe> subscribe = subscribeRepository
                .findBySubscriberIdAndSubscribedId(subscriber.getId(), subscribed.getId());
        return subscribe.get();
    }

    @Override
    public void update(Subscribe subscribe) {
        subscribeRepository.save(subscribe);
    }

    @Override
    public List<Subscribe> getSubscribes(Long id) {
        return subscribeRepository.findAllBySubscriberId(id);
    }
}
