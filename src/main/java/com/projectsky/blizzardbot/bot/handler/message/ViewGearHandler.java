package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;

@Component("viewGearMessageHandler")
@Slf4j
public class ViewGearHandler implements BotCommandHandler {

    private final GearService gearService;
    private final MarkupService markupService;
    private final MessageService messageService;
    private final Map<Long, UserState> userStates;

    public ViewGearHandler(GearService gearService,
                           MarkupService markupService,
                           MessageService messageService,
                           @Qualifier("FSM") Map<Long, UserState> userStates) {
        this.gearService = gearService;
        this.markupService = markupService;
        this.messageService = messageService;
        this.userStates = userStates;
    }

    @Override
    public boolean supports(Update update) {
        if(!update.hasMessage() || !update.getMessage().hasText()){
            log.warn("Has no message for user: [{}] in [{}]", update.getMessage().getFrom().getUserName(), ViewGearHandler.class.getSimpleName());

            return false;
        }

        String message = update.getMessage().getText();
        return ButtonText.VIEW_GEAR.equalsIgnoreCase(message);
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        List<Gear> userGears = gearService.getUserGears(userId);

        messageService.sendMessageWithInlineKeyboard(
                chatId,
                BotResponses.GEAR_LIST_USER,
                markupService.buildMarkupForGear(userGears, "toggle_gear_")

        );

        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.ACTION_CHOICE,
                markupService.buildReplyKeyboardGearMode(userId)
        );

        userStates.put(userId, UserState.GEAR_MODE);
    }
}
