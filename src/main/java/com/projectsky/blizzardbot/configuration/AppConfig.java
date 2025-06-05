package com.projectsky.blizzardbot.configuration;

import com.projectsky.blizzardbot.bot.handler.callback.CallbackQueryHandler;
import com.projectsky.blizzardbot.bot.handler.callback.ToggleGearHandler;
import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.util.CallbackCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean(name = "reminderExecutor", destroyMethod = "shutdown")
    public ExecutorService reminderExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "FSM")
    public Map<Long, UserState> userStates() {
        return new HashMap<>();
    }
}
