package com.projectsky.blizzardbot.bot.handler.callback;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.CallbackCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToggleGearHandler implements CallbackQueryHandler {

    private final GearService gearService;
    private final MessageService messageService;
    private final MarkupService markupService;

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith(CallbackCommands.TOGGLE_GEAR);
    }

    @Override
    public void handle(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        Long gearId = Long.parseLong(callbackQuery.getData().replace(CallbackCommands.TOGGLE_GEAR, ""));
        gearService.toggleGearStatus(userId, gearId);

        List<Gear> userGears = gearService.getUserGears(userId);

        messageService.sendMessageWithEditKeyboard(
                chatId,
                messageId,
                markupService.buildMarkupForGear(userGears, CallbackCommands.TOGGLE_GEAR)
        );
        log.info("User: [{}] toggled gear status", userId);
    }
}
