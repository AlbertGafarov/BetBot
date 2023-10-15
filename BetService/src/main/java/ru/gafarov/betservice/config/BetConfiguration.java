package ru.gafarov.betservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gafarov.bet.grpcInterface.BetServiceGrpc;
import ru.gafarov.betservice.model.ChangeStatusBetRules;
import ru.gafarov.betservice.repository.ChangeStatusBetRulesRepository;

import java.util.List;

@Configuration
public class BetConfiguration {

    @Autowired
    ChangeStatusBetRulesRepository changeStatusBetRulesRepository;

    @Bean
    public List<ChangeStatusBetRules> statusBetList(){
        return changeStatusBetRulesRepository.findAll();
    }

    @Bean
    public BetServiceGrpc.BetServiceBlockingStub grpcStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return BetServiceGrpc.newBlockingStub(managedChannel);
    }
}
