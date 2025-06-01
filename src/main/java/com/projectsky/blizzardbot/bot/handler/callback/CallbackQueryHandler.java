package com.projectsky.blizzardbot.bot.handler.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {

    boolean supports(CallbackQuery callbackQuery);
    void handle(CallbackQuery callbackQuery);
}
