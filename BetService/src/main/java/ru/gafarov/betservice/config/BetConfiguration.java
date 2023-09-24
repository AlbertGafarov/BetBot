package ru.gafarov.betservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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


}
