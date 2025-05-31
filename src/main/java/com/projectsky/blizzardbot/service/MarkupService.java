package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

public interface MarkupService {

    InlineKeyboardMarkup buildMarkupForGear(List<Gear> userGears, String callbackDataType);
    InlineKeyboardMarkup buildMarkupForUsers(List<User> users, String callbackDataType);
    ReplyKeyboardMarkup buildReplyKeyboardMarkup(Long userId);
    InlineKeyboardMarkup buildMarkupForAgreement(Long telegramId, String callbackDataType);
    InlineKeyboardMarkup buildMarkupForChargedUsers(List<User> users, String callbackDataType);
}
