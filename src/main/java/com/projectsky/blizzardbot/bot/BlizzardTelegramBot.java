package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.configuration.BotProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class BlizzardTelegramBot implements SpringLongPollingBot {

    private final BotProperties botProperties;
    private final UpdateConsumer updateConsumer;

    public BlizzardTelegramBot(BotProperties botProperties,
                               UpdateConsumer updateConsumer) {
        this.botProperties = botProperties;
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
