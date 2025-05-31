package com.projectsky.blizzardbot.service;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface MessageService {

    @SneakyThrows
    void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard);

    void sendMessageWithEditKeyboard(Long chatId, Integer messageId, InlineKeyboardMarkup keyboard);

    void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard);

    void sendMessageHideKeyboard(Long chatId, String text);

    void sendMessageWithCancelKeyboard(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup);
}