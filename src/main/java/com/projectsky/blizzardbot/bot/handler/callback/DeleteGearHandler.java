package com.projectsky.blizzardbot.bot.handler.callback;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.CallbackCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteGearHandler implements CallbackQueryHandler {

    private final GearService gearService;
    private final MessageService messageService;
    private final MarkupService markupService;

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith(CallbackCommands.DELETE_GEAR);
    }

    @Override
    public void handle(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long gearId = Long.parseLong(callbackQuery.getData().replace(CallbackCommands.DELETE_GEAR, ""));

        gearService.removeGear(userId, gearId);

        List<Gear> userGears = gearService.getUserGears(userId);

        if(userGears.isEmpty()){
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.GEAR_REMOVED_ALL,
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            return;
        }

        messageService.sendMessageWithEditKeyboard(
                chatId,
                messageId,
                markupService.buildMarkupForGearDeletion(userGears, CallbackCommands.DELETE_GEAR)
        );
        log.info("User: [{}] deleted item", callbackQuery.getFrom().getUserName());
    }
}
