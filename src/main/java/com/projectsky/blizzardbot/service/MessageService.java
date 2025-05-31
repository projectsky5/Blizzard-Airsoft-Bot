package com.projectsky.blizzardbot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface MessageService {

    void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard);
    void sendMessageHideKeyboard(Long chatId, String text);
    void sendMessage(Long chatId, String messageText);
}