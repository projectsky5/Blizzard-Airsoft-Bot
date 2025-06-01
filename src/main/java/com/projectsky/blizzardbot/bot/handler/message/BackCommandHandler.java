package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;

@Component
public class BackCommandHandler implements BotCommandHandler {

    private final MarkupService markupService;
    private final MessageService messageService;
    private final Map<Long, UserState> userStates;

    public BackCommandHandler(MarkupService markupService,
                              MessageService messageService,
                              @Qualifier("FSM") Map<Long, UserState> userStates) {
        this.markupService = markupService;
        this.messageService = messageService;
        this.userStates = userStates;
    }

    @Override
    public boolean supports(Update update) {
        if(!update.hasMessage() || !update.getMessage().hasText()) return false;

        String message = update.getMessage().getText();
        return ButtonText.BACK.equalsIgnoreCase(message);
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        userStates.put(userId, UserState.NONE);

        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.BACK_TO_MAIN,
                markupService.buildReplyKeyboardMarkup(userId)
        );
    }
}
