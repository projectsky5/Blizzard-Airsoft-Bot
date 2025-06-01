package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;

@Component
public class RegisterInputHandler implements BotCommandHandler{

    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;
    private final Map<Long, UserState> userStates;

    public RegisterInputHandler(UserService userService,
                                MessageService messageService,
                                MarkupService markupService,
                                @Qualifier("FSM") Map<Long, UserState> userStates) {
        this.userService = userService;
        this.messageService = messageService;
        this.markupService = markupService;
        this.userStates = userStates;
    }

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return false;

        Long userId = update.getMessage().getFrom().getId();
        boolean isUnregistered = userService.findById(userId).isEmpty();
        boolean isEntering = userStates.getOrDefault(userId, UserState.NONE) == UserState.ENTERING_CALLNAME;

        return isUnregistered && isEntering;
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String callName = message.getText();

        userService.createUser(userId, callName);
        userStates.put(userId, UserState.NONE);

        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.WELCOME.formatted(callName),
                markupService.buildReplyKeyboardMarkup(userId)
        );
    }
}
