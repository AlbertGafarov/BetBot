package ru.gafarov.betservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GrpcServerStart {
    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(GrpcServerStart.class).run(args);
    }
}
