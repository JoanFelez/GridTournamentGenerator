package com.gridpadel.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.gridpadel.infrastructure",
    "com.gridpadel.application"
})
public class InfrastructureConfig {
}
