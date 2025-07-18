package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.bot.handler.callback.CallbackQueryHandler;
import com.projectsky.blizzardbot.bot.handler.message.BotCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
@Slf4j
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final List<BotCommandHandler> handlers;
    private final Map<String, CallbackQueryHandler> queryHandlers;

    public UpdateConsumer(List<BotCommandHandler> handlers,
                          @Qualifier("callback") Map<String, CallbackQueryHandler> queryHandlers) {
        this.handlers = handlers;
        this.queryHandlers = queryHandlers;
    }

    @Override
    public void consume(Update update) {
        String username = update.getMessage().getFrom().getUserName();
        if (update.hasMessage() && update.getMessage().hasText()) {
            for (BotCommandHandler handler : handlers) {
                if(handler.supports(update)) {
                    handler.handle(update);
                    log.info("Handled method: [{}] for user: [{}]", handler.getClass().getSimpleName(), username);
                    return;
                }
            }
        }

        if(update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            for (String prefix : queryHandlers.keySet()) {
                if(data.startsWith(prefix)) {
                    queryHandlers.get(prefix).handle(callbackQuery);
                    log.info("Handled callbackQuery: [{}] for user: [{}]", queryHandlers.get(prefix).getClass().getSimpleName(), username);
                    return;
                }
            }
        }
    }
}
