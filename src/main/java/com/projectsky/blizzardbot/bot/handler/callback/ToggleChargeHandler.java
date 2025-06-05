package com.projectsky.blizzardbot.bot.handler.callback;

import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.CallbackCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToggleChargeHandler implements CallbackQueryHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith(CallbackCommands.TOGGLE_CHARGE);
    }

    @Override
    public void handle(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        userService.toggleChargeStatus(userId);

        messageService.sendMessageWithEditKeyboard(
                chatId,
                messageId,
                markupService.buildMarkupForAgreement(userId, CallbackCommands.TOGGLE_CHARGE)
        );
        log.info("User: [{}] toggled charge status", callbackQuery.getFrom().getUserName());
    }
}
