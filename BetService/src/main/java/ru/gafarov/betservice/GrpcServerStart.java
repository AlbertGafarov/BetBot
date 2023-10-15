package ru.gafarov.betservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
public class GrpcServerStart {
    public static void main(String[] args) {
        System.setProperty("user.timezone", "MSK");
        new SpringApplicationBuilder().sources(GrpcServerStart.class).run(args);
    }
}
