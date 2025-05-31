package com.projectsky.blizzardbot.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class MessageServiceImpl implements MessageService {

    private final TelegramClient telegramClient;

    public MessageServiceImpl(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    @SneakyThrows
    public void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        telegramClient.execute(message);
    }

    @Override
    @SneakyThrows
    public void sendMessageHideKeyboard(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();

        telegramClient.execute(message);
    }

    @Override
    @SneakyThrows
    public void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        telegramClient.execute(message);
    }

    @Override
    @SneakyThrows
    public void sendMessageWithCancelKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        telegramClient.execute(message);
    }
}
