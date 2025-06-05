package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.factory.ButtonFactory;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarkupServiceImpl implements MarkupService {

    private final GearService gearService;
    private final UserServiceImpl userService;
    private final ButtonFactory buttonFactory;

    public MarkupServiceImpl(GearService gearService,
                             UserServiceImpl userService,
                             ButtonFactory buttonFactory) {
        this.gearService = gearService;
        this.userService = userService;
        this.buttonFactory = buttonFactory;
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
        currentRow.add(buttonFactory.createToggleGearButton(gear, callbackDataType));

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

            currentRow.add(buttonFactory.createUserStatusButton(user, callbackDataType, isReady));

            if(currentRow.size() == 2 || i == users.size() - 1) {
                buttonRows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForChargedUsers(List<User> users, String callbackDataType) {
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            boolean isCharged = userService.isAccCharged(user.getTelegramId());

            currentRow.add(buttonFactory.createUserStatusButton(user, callbackDataType, isCharged));

            if(currentRow.size() == 2 || i == users.size() - 1) {
                buttonRows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardGearMode(Long userId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(buttonFactory.createAddGearButton());
        row1.add(buttonFactory.createRemoveGearButton());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(buttonFactory.createCancelButton());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(buttonFactory.createViewGearButton());

        List<KeyboardRow> keyboard = List.of(row1, row2, row3);

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

            currentRow.add(buttonFactory.createDeleteGearButton(gear, callbackDataType));

            if(currentRow.size() == 2 || i == userGears.size() - 1) {
                buttonRows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    @Override
    public InlineKeyboardMarkup buildMarkupForAgreement(Long telegramId, String callbackDataType) {
        boolean isCharged = userService.isAccCharged(telegramId);

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(buttonFactory.createAgreementButton(telegramId, callbackDataType, isCharged));

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardMarkup(Long userId) {
        return userService.isAdmin(userId) || userService.isCommander(userId)
                ? buildCommanderKeyboard() : buildUserKeyboard();
    }

    private ReplyKeyboardMarkup buildCommanderKeyboard(){
        KeyboardRow row1 = new KeyboardRow();
        row1.add(buttonFactory.createCheckMyGearButton());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(buttonFactory.createTeamStatusButton());
        row2.add(buttonFactory.createChargeStatusButton());

        List<KeyboardRow> keyboard = List.of(row1, row2);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    private ReplyKeyboardMarkup buildUserKeyboard(){
        KeyboardRow row1 = new KeyboardRow();
        row1.add(buttonFactory.createCheckMyGearButton());

        List<KeyboardRow> keyboard = List.of(row1);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    @Override
    public ReplyKeyboardMarkup buildReplyKeyboardCancelMarkup(Long userId) {
        KeyboardRow row = new KeyboardRow();
        row.add(buttonFactory.createCancelButton());

        List<KeyboardRow> keyboard = List.of(row);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }
}
