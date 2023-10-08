package ru.gafarov.betservice.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;

@SpringBootApplication
@EnableConfigurationProperties(ConfigMap.class)
public class BetTelegramBotApplication {

    public static void main(String[] args) {
        System.setProperty("user.timezone", "MSK");
        SpringApplication.run(BetTelegramBotApplication.class, args);
    }
}
