package ru.gafarov.betservice.telegram.bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("service")
public class ConfigMap {
    private Bot bot;

    @Getter
    @Setter
    public static class Bot {
        private String name;
        private String token;
    }
}
