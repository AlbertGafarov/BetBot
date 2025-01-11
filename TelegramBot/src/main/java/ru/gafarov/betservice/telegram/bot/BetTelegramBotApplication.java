package ru.gafarov.betservice.telegram.bot;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.gafarov.betservice.telegram.bot.config.ConfigMap;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(ConfigMap.class)
public class BetTelegramBotApplication {

    public static void main(String[] args) {
        System.setProperty("user.timezone", "MSK");

        SpringApplicationBuilder app = new SpringApplicationBuilder(BetTelegramBotApplication.class)
                .web(WebApplicationType.NONE);
        app.build().addListeners(new ApplicationPidFileWriter("/application/TelegramBot/shutdown.pid"));
        app.run(args);
    }
}
