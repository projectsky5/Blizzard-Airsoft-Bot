package com.projectsky.blizzardbot.bot.handler.message;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotCommandHandler {

    //Проверяет, подходит ли хэндлер для обработки текущего апдейта
    boolean supports(Update update);

    void handle(Update update);
}
