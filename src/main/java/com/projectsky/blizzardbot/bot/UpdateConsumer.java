package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.configuration.BotProperties;
import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.UserService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;
    private final GearService gearService;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UpdateConsumer(BotProperties botProperties,
                          UserService userService,
                          GearService gearService) {
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
        this.userService = userService;
        this.gearService = gearService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long userId = update.getMessage().getFrom().getId();
            Long chatId = update.getMessage().getChatId();

            boolean isRegistered = userService.findById(userId).isPresent();

            if("/start".equals(message)) {
                if(isRegistered) {
                    sendMessageWithKeyboard(chatId, "Добро пожаловать %s!"
                                    .formatted(userService.findById(userId).get().getCallName()),
                            buildReplyKeyboard(userId));
                } else{
                    userStates.put(userId, UserState.ENTERING_CALLNAME);
                    sendMessageHideKeyboard(chatId, """
                        Привет! Введи свой позывной, чтобы начать работу.
                        """);
                }
            } else if(!isRegistered && userStates.getOrDefault(userId, UserState.NONE) == UserState.ENTERING_CALLNAME) {
                userService.createUser(userId, message);
                userStates.put(userId, UserState.NONE);
                sendMessageWithKeyboard(chatId, "Позывной '%s' сохранен! Добро пожаловать.".formatted(message),
                        buildReplyKeyboard(userId));

            } else if ("/add_gear".equalsIgnoreCase(message) || "Добавить предмет".equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.ADDING_GEAR);
                sendMessageWithKeyboard(chatId, "Введите название предмета", buildReplyKeyboard(userId));

            } else if(userStates.getOrDefault(userId, UserState.NONE) == UserState.ADDING_GEAR) {
                try {
                    gearService.addGear(userId, message);
                } catch (GearAlreadyExistsException e) {
                    sendMessage(chatId, "Данный элемент снаряжения уже добавлен!");
                    userStates.put(userId, UserState.NONE);
                    return;
                }
                userStates.put(userId, UserState.NONE);
                sendMessage(chatId, "Предмет '%s' успешно добавлен.".formatted(message));

            } else if("/check_gear".equalsIgnoreCase(message) || "Мое снаряжение".equalsIgnoreCase(message)) {
                List<Gear> userGears = gearService.getUserGears(userId);

                if(userGears.isEmpty()){
                    sendMessageWithKeyboard(chatId, "Снаряжение не найдено", buildReplyKeyboard(userId));
                    return;
                }

                InlineKeyboardMarkup markup = buildMarkupForGear(userGears, "toggle_gear_");

                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Список вашего снаряжения:")
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if(message.startsWith("/set_commander")) {
                if (!userService.isAdmin(userId)) {
                    sendMessage(chatId, "У тебя нет прав для назначения командира.");
                    return;
                }

                String[] parts = message.split(" ");
                if (parts.length != 2) {
                    sendMessage(chatId, "Использование: /set_commander @позывной");
                    return;
                }

                String callName = parts[1].replace("@", "");

                userService.promoteToCommander(callName);

                User promotedUser = userService.findByCallName(callName)
                                .orElseThrow(RuntimeException::new);
                sendMessage(chatId, "Пользователь @" + callName + " назначен командиром.");

                sendMessageWithKeyboard(promotedUser.getTelegramId(), "Ты был назначен командиром.", buildReplyKeyboard(userId));
            } else if("/team_status".equalsIgnoreCase(message) || "Сборы команды".equalsIgnoreCase(message)) {
                if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
                    sendMessage(chatId, "Только командир может просматривать сборы команды");
                    return;
                }

                List<User> users = userService.getAllVisibleUsers();

                if(users.isEmpty()){
                    sendMessage(chatId, "Команда пуста");
                    return;
                }

                InlineKeyboardMarkup markup = buildMarkupForUsers(users, "user_status_");

                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Готовность команды:")
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            }
            else {
                sendMessage(chatId, "Команда не распознана");
            }
        }
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Long userId = callbackQuery.getFrom().getId();
            Long chatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            if (data.startsWith("toggle_gear_")){
                Long gearId = Long.parseLong(data.replace("toggle_gear_", ""));
                gearService.toggleGearStatus(userId, gearId);

                List<Gear> userGears = gearService.getUserGears(userId);
                InlineKeyboardMarkup markup = buildMarkupForGear(userGears, "toggle_gear_");

                EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(editMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText){
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        telegramClient.execute(message);
    }

    private ReplyKeyboardMarkup buildReplyKeyboard(Long userId){
        KeyboardButton addItem = new KeyboardButton("Добавить предмет");
        KeyboardButton checkItems = new KeyboardButton("Мое снаряжение");
        KeyboardButton teamStatus = new KeyboardButton("Сборы команды");

        KeyboardRow commanderRow = new KeyboardRow();
        commanderRow.add(addItem);
        commanderRow.add(checkItems);
        commanderRow.add(teamStatus);

        KeyboardRow userRow = new KeyboardRow();
        userRow.add(addItem);
        userRow.add(checkItems);

        List<KeyboardRow> keyboard;

        if(userService.isCommander(userId) || userService.isAdmin(userId)){
            keyboard = List.of(commanderRow);
        } else {
            keyboard = List.of(userRow);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    @SneakyThrows
    private void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard){
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendMessageHideKeyboard(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();

        telegramClient.execute(message);
    }

    private InlineKeyboardMarkup buildMarkupForGear(List<Gear> userGears, String callbackDataType){
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < userGears.size(); i++) {
            Gear gear = userGears.get(i);

            String text = "%s %s".formatted(gear.getItemName(), gear.isReady() ? "✅" : "❌");
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
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }

    private InlineKeyboardMarkup buildMarkupForUsers(List<User> users, String callbackDataType){
        List<InlineKeyboardRow> buttonRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            boolean isReady = gearService.isFullyEquipped(user.getTelegramId());

            String text = "%s %s".formatted(user.getCallName(), isReady ? "✅" : "❌");
            String callbackData = callbackDataType + user.getTelegramId();

            InlineKeyboardButton toggleButton = InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callbackData + user.getTelegramId())
                    .build();

            currentRow.add(toggleButton);

            if(currentRow.size() == 2 || i == users.size() - 1) {
                buttonRows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(buttonRows)
                .build();
    }
}
