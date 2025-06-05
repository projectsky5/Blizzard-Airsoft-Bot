package com.projectsky.blizzardbot.service;

import com.google.common.util.concurrent.RateLimiter;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.CallbackCommands;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ExecutorService executor;
    private final UserServiceImpl userService;
    private final TelegramClient telegramClient;
    private final MessageService messageService;
    private final MarkupService markupService;

    public ReminderServiceImpl(
            @Qualifier("reminderExecutor") ExecutorService executor,
            UserServiceImpl userService,
            TelegramClient telegramClient,
            MarkupService markupService,
            MessageService messageService) {
        this.executor = executor;
        this.userService = userService;
        this.telegramClient = telegramClient;
        this.markupService = markupService;
        this.messageService = messageService;
    }

    @Override
    public void sendReminder() {
        List<User> users = userService.getAllVisibleUsers().stream()
                .filter(u -> !u.isAccumulatorCharged())
                .toList();
        RateLimiter limiter = RateLimiter.create(25.0); //30 msg / в секунду
        for (User user : users) {
            executor.submit(() -> {
                limiter.acquire();

                messageService.sendMessageWithInlineKeyboard(
                        user.getTelegramId(),
                        BotResponses.CHARGE_ACCUMULATOR,
                        markupService.buildMarkupForAgreement(user.getTelegramId(), CallbackCommands.TOGGLE_CHARGE)
                );
            });
        }
    }
}
