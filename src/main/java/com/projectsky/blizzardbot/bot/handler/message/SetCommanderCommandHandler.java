package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class SetCommanderCommandHandler implements BotCommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return false;

        String message = update.getMessage().getText();
        return message.startsWith("/set_commander");
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        if(!userService.isAdmin(userId)) {
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.NO_ACCESS,
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            return;
        }

        String[] parts = text.split(" ");
        if(parts.length != 2){
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.INVALID_COMMAND,
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            return;
        }

        String callName = parts[1].replace("@", "");

        userService.promoteToCommander(callName);

        User promotedUser = userService.findByCallName(callName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.USER_PROMOTED.formatted(callName),
                markupService.buildReplyKeyboardMarkup(userId)
        );

        messageService.sendMessageWithKeyboard(
                promotedUser.getTelegramId(),
                BotResponses.YOU_ARE_COMMANDER,
                markupService.buildReplyKeyboardMarkup(userId)
        );
    }
}
