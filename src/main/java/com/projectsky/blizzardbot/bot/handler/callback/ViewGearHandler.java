package com.projectsky.blizzardbot.bot.handler.callback;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.CallbackCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

@Component("viewGearCallbackHandler")
@RequiredArgsConstructor
public class ViewGearHandler implements CallbackQueryHandler{

    private final GearService gearService;
    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;


    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith(CallbackCommands.SHOW_USER_GEAR);
    }

    @Override
    public void handle(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long targetTelegramId = Long.parseLong(callbackQuery.getData().replace(CallbackCommands.SHOW_USER_GEAR, ""));
        List<Gear> userGears = gearService.getUserGears(targetTelegramId);

        ReplyKeyboardMarkup keyboard = markupService.buildReplyKeyboardMarkup(userId);

        if(!userService.isAdmin(userId) && !userService.isCommander(userId)) {
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.NO_PERMISSION_VIEW_GEAR,
                    keyboard
            );
            return;
        }

        if(userGears.isEmpty()){
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.USER_HAS_NO_GEAR,
                    keyboard
            );
            return;
        }

        messageService.sendMessageWithInlineKeyboard(
                chatId,
                BotResponses.GEAR_LIST_COMMANDER,
                markupService.buildMarkupForGear(userGears, CallbackCommands.SHOW_USER_GEAR)
        );
    }
}
