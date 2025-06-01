package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
public class StartCommandHandler implements BotCommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;
    private final Map<Long, UserState> userStates;

    public StartCommandHandler(UserService userService,
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
        return update.hasMessage()
                && update.getMessage().hasText()
                && update.getMessage().getText().startsWith("/start");
    }

    @Override
    public void handle(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        boolean isRegistered = userService.findById(userId).isPresent();

        if (isRegistered) {
            User user = userService.findById(userId).get();
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.WELCOME.formatted(user.getCallName()),
                    markupService.buildReplyKeyboardMarkup(userId)
            );
        } else {
            userStates.put(userId, UserState.ENTERING_CALLNAME);
            messageService.sendMessageHideKeyboard(
                    chatId,
                    BotResponses.ENTER_CALLSIGN
            );
        }
    }
}
