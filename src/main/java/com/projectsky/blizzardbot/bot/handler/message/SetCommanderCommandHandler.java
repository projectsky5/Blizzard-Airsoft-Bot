package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import com.projectsky.blizzardbot.util.BotResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetCommanderCommandHandler implements BotCommandHandler {

    private final UserService userService;
    private final MessageService messageService;
    private final MarkupService markupService;

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()){
            log.warn("Has no message for user: [{}] in [{}]", update.getMessage().getFrom().getUserName(), SetCommanderCommandHandler.class.getSimpleName());
            return false;
        }

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
            log.warn("User [{}] has no permission to set the commander", message.getFrom().getUserName());
            return;
        }

        String[] parts = text.split(" ");
        if(parts.length != 2){
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.INVALID_COMMAND,
                    markupService.buildReplyKeyboardMarkup(userId)
            );
            log.warn("User: [{}] used invalid command: [{}]", message.getFrom().getUserName(), text);
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
