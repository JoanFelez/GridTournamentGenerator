package com.gridpadel.infrastructure.config;

import com.gridpadel.domain.service.BracketGenerationService;
import com.gridpadel.domain.service.MatchAdvancementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.gridpadel.infrastructure",
    "com.gridpadel.application"
})
public class InfrastructureConfig {

    @Bean
    public BracketGenerationService bracketGenerationService() {
        return new BracketGenerationService();
    }

    @Bean
    public MatchAdvancementService matchAdvancementService() {
        return new MatchAdvancementService();
    }
}
