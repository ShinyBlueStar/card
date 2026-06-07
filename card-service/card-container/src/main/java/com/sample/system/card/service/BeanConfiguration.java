package com.sample.system.card.service;

import com.sample.system.card.service.domain.CardDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public com.sample.system.card.service.domain.CardDomainService cardDomainService() {
        return new CardDomainServiceImpl();
    }
}