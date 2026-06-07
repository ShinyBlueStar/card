package com.sample.system.card.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories(basePackages = { "com.sample.system.card.service.dataaccess.card.repository"})
@EntityScan(basePackages = { "com.sample.system.card.service.dataaccess.card.entity" })
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.sample.system.card",
        "com.sample.system.card.service.dataaccess.card.adapter",
        "com.sample.system.card.service.dataaccess.card.mapper",
        "com.sample.system.card.service.application.rest"
})
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class CardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardServiceApplication.class, args);
    }
}
