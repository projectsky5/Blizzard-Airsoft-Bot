package com.projectsky.blizzardbot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean(name = "reminderExecutor", destroyMethod = "shutdown")
    public ExecutorService reminderExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
