package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamStatusCommandHandler implements BotCommandHandler {

    private final UserService userService;
    private final MarkupService markupService;
    private final MessageService messageService;

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return false;

        String message = update.getMessage().getText();
        return "/team_status".equalsIgnoreCase(message) || ButtonText.TEAM_STATUS.equalsIgnoreCase(message);
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        //Проверка роли
        if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.COMMANDER_ONLY.formatted(message.getText()),
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            return;
        }

        List<User> users = userService.findAllWithGears();

        if(users.isEmpty()){
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.TEAM_EMPTY,
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            return;
        }

        messageService.sendMessageWithInlineKeyboard(
                chatId,
                BotResponses.TEAM_STATUS,
                markupService.buildMarkupForUsers(users, "show_gear_")
        );
    }
}
