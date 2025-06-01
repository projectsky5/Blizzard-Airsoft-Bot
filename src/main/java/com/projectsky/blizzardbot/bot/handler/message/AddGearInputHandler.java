package com.projectsky.blizzardbot.bot.handler.message;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Map;

@Component
public class AddGearInputHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final GearService gearService;
    private final MarkupService markupService;
    private final Map<Long, UserState> userStates;

    public AddGearInputHandler(MessageService messageService,
                               GearService gearService,
                               MarkupService markupService,
                               @Qualifier("FSM") Map<Long, UserState> userStates) {
        this.messageService = messageService;
        this.gearService = gearService;
        this.markupService = markupService;
        this.userStates = userStates;
    }

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return false;

        Long userId = update.getMessage().getFrom().getId();
        return userStates.getOrDefault(userId, UserState.NONE) == UserState.ADDING_GEAR;
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        ReplyKeyboardMarkup mainKeyboard = markupService.buildReplyKeyboardMarkup(userId);

        // Обработка кнопки "Назад"
        if(ButtonText.CANCEL.equalsIgnoreCase(text)){
            userStates.put(userId, UserState.NONE);
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.CANCEL_ADD,
                    mainKeyboard
            );
            return;
        }

        // Добавление снаряжения
        try {
            gearService.addGear(userId, text);
        } catch (GearAlreadyExistsException e) {
            //Уведомление о том что предмет уже существует
            userStates.put(userId, UserState.NONE);
            messageService.sendMessageWithKeyboard(
                    chatId,
                    BotResponses.GEAR_EXISTS,
                    mainKeyboard
            );
            return;
        }

        // Уведомление о успешном добавлении
        userStates.put(userId, UserState.GEAR_MODE);
        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.GEAR_ADDED.formatted(text),
                markupService.buildReplyKeyboardGearMode(userId)
        );
    }
}
