package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarkupServiceImpl implements MarkupService {

    private final GearService gearService;
    private final UserServiceImpl userService;

    public MarkupServiceImpl(GearService gearService,
                             UserServiceImpl userService) {
        this.gearService = gearService;
        this.userService = userService;
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForGear(List<Gear> userGears, String callbackDataType) {
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < userGears.size(); i++) {
            Gear gear = userGears.get(i);

            String text = "%s %s".formatted(gear.getItemName(), gear.isReady() ? "✅" : "❌");
            currentRow = getInlineKeyboardButtons(userGears, callbackDataType, buttonRows, currentRow, i, gear, text);
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @NotNull
    private InlineKeyboardRow getInlineKeyboardButtons(List<Gear> userGears, String callbackDataType, List<InlineKeyboardRow> buttonRows, InlineKeyboardRow currentRow, int i, Gear gear, String text) {
        String callbackData = callbackDataType + gear.getId();

        InlineKeyboardButton toggleButton = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();

        currentRow.add(toggleButton);

        if(currentRow.size() == 2 || i == userGears.size() - 1) {
            buttonRows.add(currentRow);
            currentRow = new InlineKeyboardRow();
        }
        return currentRow;
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForUsers(List<User> users, String callbackDataType) {
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            boolean isReady = gearService.isFullyEquipped(user.getTelegramId());

            currentRow = getInlineKeyboardButtons(users, callbackDataType, buttonRows, currentRow, i, user, isReady);
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @NotNull
    private InlineKeyboardRow getInlineKeyboardButtons(List<User> users, String callbackDataType, List<InlineKeyboardRow> buttonRows, InlineKeyboardRow currentRow, int i, User user, boolean isReady) {
        String text = "%s %s".formatted(user.getCallName(), isReady ? "✅" : "❌");
        String callbackData = callbackDataType + user.getTelegramId();

        InlineKeyboardButton toggleButton = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();

        currentRow.add(toggleButton);

        if(currentRow.size() == 2 || i == users.size() - 1) {
            buttonRows.add(currentRow);
            currentRow = new InlineKeyboardRow();
        }
        return currentRow;
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForChargedUsers(List<User> users, String callbackDataType) {
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            boolean isCharged = userService.isAccCharged(user.getTelegramId());

            currentRow = getInlineKeyboardButtons(users, callbackDataType, buttonRows, currentRow, i, user, isCharged);
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardGearMode(Long userId) {
        KeyboardButton back = new KeyboardButton("Назад");
        KeyboardButton add = new KeyboardButton("Добавить предмет");
        KeyboardButton remove = new KeyboardButton("Удалить предмет");
        KeyboardButton gear = new KeyboardButton("Посмотреть снаряжение");

        KeyboardRow row1 = new KeyboardRow();
        row1.add(add);
        row1.add(remove);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(back);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(gear);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForGearDeletion(List<Gear> userGears, String callbackDataType) {
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < userGears.size(); i++) {
            Gear gear = userGears.get(i);

            String text = "%s".formatted(gear.getItemName());
            currentRow = getInlineKeyboardButtons(userGears, callbackDataType, buttonRows, currentRow, i, gear, text);
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForAgreement(Long telegramId, String callbackDataType) {
        boolean isCharged = userService.isAccCharged(telegramId);

        String text = "%s".formatted(isCharged ? "✅" : "❌");
        String callbackData = callbackDataType + telegramId;

        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(new InlineKeyboardRow(List.of(button)));

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardMarkup(Long userId) {
        KeyboardButton checkItems = new KeyboardButton("Мое снаряжение");
        KeyboardButton teamStatus = new KeyboardButton("Сборы команды");
        KeyboardButton chargeStatus = new KeyboardButton("Состояние аккумуляторов");

        List<KeyboardRow> keyboard = new ArrayList<>();

        if(userService.isCommander(userId) || userService.isAdmin(userId)){
            KeyboardRow row1 = new KeyboardRow();
            row1.add(checkItems);
            keyboard.add(row1);

            KeyboardRow row2 = new KeyboardRow();
            row2.add(teamStatus);
            row2.add(chargeStatus);
            keyboard.add(row2);
        } else {
            KeyboardRow row = new KeyboardRow();
            row.add(checkItems);
            keyboard.add(row);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardCancelMarkup(Long userId) {
        KeyboardButton cancel = new KeyboardButton("Назад");

        KeyboardRow row = new KeyboardRow(cancel);
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }
}
