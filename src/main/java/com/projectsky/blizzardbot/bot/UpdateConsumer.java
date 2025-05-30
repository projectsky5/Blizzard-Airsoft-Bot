package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.configuration.BotProperties;
import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.UserService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;
    private final GearService gearService;

    private final Set<Long> awaitingCallNames = new HashSet<>();
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
                            buildReplyKeyboard()
                            );
                } else{
                    awaitingCallNames.add(userId);
                    sendMessage(chatId, """
                        Привет! Введи свой позывной, чтобы начать работу.
                        """);
                }
            } else if(!isRegistered && awaitingCallNames.contains(chatId)) {
                userService.createUser(userId, message);
                awaitingCallNames.remove(chatId);
                sendMessageWithKeyboard(chatId, "Позывной '%s' сохранен! Добро пожаловать.".formatted(message),
                        buildReplyKeyboard());

            } else if ("/add_gear".equalsIgnoreCase(message) || "Добавить предмет".equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.ADDING_GEAR);
                sendMessage(chatId, "Введите название предмета");

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
                sendMessage(chatId, buildGearList(userGears));
            }
            else if(message.startsWith("/set_commander")) {
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
                sendMessage(chatId, "Пользователь @" + callName + " назначен командиром.");
            } else {
                sendMessage(chatId, "Команда не распознана");
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

    private ReplyKeyboardMarkup buildReplyKeyboard(){
        KeyboardButton addItem = new KeyboardButton("Добавить предмет");
        KeyboardButton checkItems = new KeyboardButton("Мое снаряжение");

        KeyboardRow row = new KeyboardRow();
        row.add(addItem);
        row.add(checkItems);

        List<KeyboardRow> keyboard = List.of(row);

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

    private String buildGearList(List<Gear> gears){
        return gears.stream()
                .map(Gear::getItemName)
                .collect(Collectors.joining("\n"));
    }
}
