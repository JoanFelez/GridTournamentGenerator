package com.gridpadel.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gridpadel.domain.service.BracketEditService;
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
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    @Bean
    public BracketGenerationService bracketGenerationService() {
        return new BracketGenerationService();
    }

    @Bean
    public MatchAdvancementService matchAdvancementService() {
        return new MatchAdvancementService();
    }

    @Bean
    public BracketEditService bracketEditService() {
        return new BracketEditService();
    }
}
