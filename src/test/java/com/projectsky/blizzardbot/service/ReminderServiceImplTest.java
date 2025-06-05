package com.projectsky.blizzardbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    ExecutorService executorService;

    @Mock
    UserServiceImpl userService;

    @Mock
    TelegramClient telegramClient;

    @Mock
    MarkupService markupService;

    @InjectMocks
    ReminderServiceImpl service;

}