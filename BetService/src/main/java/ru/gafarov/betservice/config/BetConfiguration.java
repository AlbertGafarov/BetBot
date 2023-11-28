package ru.gafarov.betservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.betservice.entity.BetFinalStatusRule;
import ru.gafarov.betservice.entity.BetStatusRule;
import ru.gafarov.betservice.entity.ChangeStatusBetRule;
import ru.gafarov.betservice.repository.BetFinalStatusRuleRepository;
import ru.gafarov.betservice.repository.BetStatusRuleRepository;
import ru.gafarov.betservice.repository.ChangeStatusBetRuleRepository;

import java.util.List;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class BetConfiguration {

    private final ChangeStatusBetRuleRepository changeStatusBetRuleRepository;
    private final BetFinalStatusRuleRepository betFinalStatusRuleRepository;
    private final BetStatusRuleRepository betStatusRuleRepository;

    @Bean
    public List<ChangeStatusBetRule> statusBetList(){
        return changeStatusBetRuleRepository.findAll();
    }

    @Bean
    public List<BetFinalStatusRule> betFinalStatusRulesList(){
        return betFinalStatusRuleRepository.findAll();
    }
    @Bean
    public List<BetStatusRule> betStatusRulesList(){
        return betStatusRuleRepository.findAll();
    }

    @Bean
    public BetServiceGrpc.BetServiceBlockingStub grpcStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return BetServiceGrpc.newBlockingStub(managedChannel);
    }
}
