package com.projectsky.blizzardbot.factory;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

public interface ButtonFactory {

    KeyboardButton createBackButton(Long userId);
    KeyboardButton createCancelButton();
    KeyboardButton createAddGearButton();
    KeyboardButton createRemoveGearButton();
    KeyboardButton createViewGearButton();
    KeyboardButton createCheckMyGearButton();
    KeyboardButton createTeamStatusButton();
    KeyboardButton createChargeStatusButton();

    InlineKeyboardButton createToggleGearButton(Gear gear, String callbackPrefix);
    InlineKeyboardButton createDeleteGearButton(Gear gear, String callbackPrefix);

    InlineKeyboardButton createUserStatusButton(User user, String callbackPrefix, boolean status);
    InlineKeyboardButton createAgreementButton(Long telegramId, String callbackPrefix, boolean isCharged);
}
