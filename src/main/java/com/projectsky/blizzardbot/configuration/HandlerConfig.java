package com.projectsky.blizzardbot.configuration;

import com.projectsky.blizzardbot.bot.handler.callback.*;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.CallbackCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HandlerConfig {

    private final UserService userService;
    private final GearService gearService;
    private final MarkupService markupService;
    private final MessageService messageService;

    @Bean(name = "callback")
    public Map<String, CallbackQueryHandler> callbackQueryHandlers() {
        return Map.of(
                CallbackCommands.TOGGLE_GEAR, new ToggleGearHandler(gearService, messageService, markupService),
                CallbackCommands.DELETE_GEAR, new DeleteGearHandler(gearService, messageService, markupService),
                CallbackCommands.SHOW_USER_GEAR, new ViewGearHandler(gearService, userService, messageService, markupService),
                CallbackCommands.TOGGLE_CHARGE, new ToggleChargeHandler(userService, messageService, markupService)
        );
    }
}
