package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;

@Component
@Slf4j
public class AddGearCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final MarkupService markupService;
    private final Map<Long, UserState> userStates;

    public AddGearCommandHandler(MessageService messageService,
                                 MarkupService markupService,
                                 @Qualifier("FSM") Map<Long, UserState> userStates) {
        this.messageService = messageService;
        this.markupService = markupService;
        this.userStates = userStates;
    }

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()){
            log.warn("Has no message for user: [{}] in [{}]", update.getMessage().getFrom().getUserName(), AddGearCommandHandler.class.getSimpleName());
            return false;
        }

        String message = update.getMessage().getText();

        return "/add_gear".equalsIgnoreCase(message) || ButtonText.ADD_GEAR.equalsIgnoreCase(message);
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        userStates.put(userId, UserState.ADDING_GEAR);

        messageService.sendMessageWithCancelKeyboard(
                chatId,
                BotResponses.START_GEAR_ADD,
                markupService.buildReplyKeyboardCancelMarkup(userId)
        );
    }
}
