package ru.gafarov.betservice.telegram.bot.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gafarov.bet.grpcInterface.*;

@Configuration
public class BotConfiguration {

    @Bean
    public BetServiceGrpc.BetServiceBlockingStub grpcStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return BetServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public InfoServiceGrpc.InfoServiceBlockingStub grpcInfoStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return InfoServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public FriendServiceGrpc.FriendServiceBlockingStub grpcFriendStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return FriendServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public BotMessageServiceGrpc.BotMessageServiceBlockingStub grpcBotMessageStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return BotMessageServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub grpcUserStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return UserServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public DrBetServiceGrpc.DrBetServiceBlockingStub grpcDrBetStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return DrBetServiceGrpc.newBlockingStub(managedChannel);
    }
    @Bean
    public SecretKeyServiceGrpc.SecretKeyServiceBlockingStub grpcSecretKeyStub(@Value("${service.host}") String host, @Value("${service.port}") int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return SecretKeyServiceGrpc.newBlockingStub(managedChannel);
    }
}
