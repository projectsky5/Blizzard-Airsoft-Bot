package com.projectsky.blizzardbot.factory;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.util.ButtonText;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Component
public class ButtonFactoryImpl implements ButtonFactory {

    @Override
    public KeyboardButton createBackButton(Long userId) {
        return new KeyboardButton(ButtonText.BACK);
    }

    @Override
    public KeyboardButton createCancelButton() {
        return new KeyboardButton(ButtonText.CANCEL);
    }

    @Override
    public KeyboardButton createAddGearButton() {
        return new KeyboardButton(ButtonText.ADD_GEAR);
    }

    @Override
    public KeyboardButton createRemoveGearButton() {
        return new KeyboardButton(ButtonText.REMOVE_GEAR);
    }

    @Override
    public KeyboardButton createViewGearButton() {
        return new KeyboardButton(ButtonText.VIEW_GEAR);
    }

    @Override
    public KeyboardButton createCheckMyGearButton() {
        return new KeyboardButton(ButtonText.MY_GEAR);
    }

    @Override
    public KeyboardButton createTeamStatusButton() {
        return new KeyboardButton(ButtonText.TEAM_STATUS);
    }

    @Override
    public KeyboardButton createChargeStatusButton() {
        return new KeyboardButton(ButtonText.ACCUMULATOR_STATUS);
    }

    @Override
    public InlineKeyboardButton createToggleGearButton(Gear gear, String callbackPrefix) {
        String text = "%s %s".formatted(gear.getItemName(), gear.isReady() ? "✅" : "❌");
        String callbackData = callbackPrefix + gear.getId();

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    @Override
    public InlineKeyboardButton createDeleteGearButton(Gear gear, String callbackPrefix) {
        String callbackData = callbackPrefix + gear.getId();

        return InlineKeyboardButton.builder()
                .text(gear.getItemName())
                .callbackData(callbackData)
                .build();
    }

    @Override
    public InlineKeyboardButton createUserStatusButton(User user, String callbackPrefix, boolean status) {
        String text = "%s %s".formatted(user.getCallName(), status ? "✅" : "❌");
        String callbackData = callbackPrefix + user.getTelegramId();

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    @Override
    public InlineKeyboardButton createAgreementButton(Long telegramId, String callbackPrefix, boolean isCharged) {
        String text = isCharged ? "✅" : "❌";
        String callbackData = callbackPrefix + telegramId;

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
