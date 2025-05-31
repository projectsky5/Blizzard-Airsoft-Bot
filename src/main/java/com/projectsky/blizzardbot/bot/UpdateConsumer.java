package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;
    private final GearService gearService;
    private final MessageService messageService;
    private final MarkupService markupService;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UpdateConsumer(TelegramClient telegramClient,
                          UserService userService,
                          GearService gearService,
                          MessageService messageService,
                          MarkupService markupService) {
        this.telegramClient = telegramClient;
        this.userService = userService;
        this.gearService = gearService;
        this.messageService = messageService;
        this.markupService = markupService;
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
                    sendWelcome(chatId, userId);
                } else{
                    sendStartMessage(userId, chatId);
                }
            } else if(!isRegistered && userStates.getOrDefault(userId, UserState.NONE) == UserState.ENTERING_CALLNAME) {
                register(userId, message, chatId);

            } else if ("/add_gear".equalsIgnoreCase(message) || "Добавить предмет".equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.ADDING_GEAR);
                messageService.sendMessageWithCancelKeyboard(chatId, "Введите название предмета", markupService.buildReplyKeyboardCancelMarkup(userId));
            } else if(userStates.getOrDefault(userId, UserState.NONE) == UserState.ADDING_GEAR) {
                try {
                    if("Назад".equals(message)) {
                        messageService.sendMessageWithKeyboard(chatId, "Добавление отменено", markupService.buildReplyKeyboardMarkup(userId));
                        userStates.put(userId, UserState.NONE);
                        return;
                    }

                    gearService.addGear(userId, message);
                } catch (GearAlreadyExistsException e) {
                    messageService.sendMessage(chatId, "Данный элемент снаряжения уже добавлен!");
                    userStates.put(userId, UserState.NONE);
                    return;
                }
                userStates.put(userId, UserState.GEAR_MODE);
                messageService.sendMessageWithKeyboard(chatId, "Предмет '%s' успешно добавлен.".formatted(message), markupService.buildReplyKeyboardGearMode(userId));
            } else if("/check_gear".equalsIgnoreCase(message) || "Мое снаряжение".equalsIgnoreCase(message)) {
                List<Gear> userGears = gearService.getUserGears(userId);

                InlineKeyboardMarkup markup = markupService.buildMarkupForGear(userGears, "toggle_gear_");

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

                ReplyKeyboardMarkup gearKeyboard = markupService.buildReplyKeyboardGearMode(userId);
                messageService.sendMessageWithKeyboard(chatId, "Выберите действие", gearKeyboard);
            } else if("Посмотреть снаряжение".equalsIgnoreCase(message)) {
                List<Gear> userGears = gearService.getUserGears(userId);

                InlineKeyboardMarkup markup = markupService.buildMarkupForGear(userGears, "toggle_gear_");

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
            }
            else if ("Удалить предмет".equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.REMOVING_GEAR);

                List<Gear> userGears = gearService.getUserGears(userId);
                if(userGears.isEmpty()){
                    messageService.sendMessage(chatId, "У тебя пока нет снаряжения");
                    return;
                }

                InlineKeyboardMarkup markup = markupService.buildMarkupForGearDeletion(userGears, "delete_gear_");

                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Выбери предмет для удаления:")
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            } else if(message.startsWith("/set_commander")) {
                if (!userService.isAdmin(userId)) {
                    messageService.sendMessage(chatId, "У тебя нет прав для назначения командира.");
                    return;
                }

                String[] parts = message.split(" ");
                if (parts.length != 2) {
                    messageService.sendMessage(chatId, "Использование: /set_commander @позывной");
                    return;
                }

                String callName = parts[1].replace("@", "");

                userService.promoteToCommander(callName);

                User promotedUser = userService.findByCallName(callName)
                                .orElseThrow(RuntimeException::new);
                messageService.sendMessage(chatId, "Пользователь " + callName + " назначен командиром.");

                messageService.sendMessageWithKeyboard(promotedUser.getTelegramId(), "Ты был назначен командиром.", markupService.buildReplyKeyboardMarkup(userId));
            } else if("/team_status".equalsIgnoreCase(message) || "Сборы команды".equalsIgnoreCase(message)) {
                if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
                    messageService.sendMessage(chatId, "Только командир может просматривать сборы команды");
                    return;
                }

                List<User> users = userService.getAllVisibleUsers();

                if(users.isEmpty()){
                    messageService.sendMessage(chatId, "Команда пуста");
                    return;
                }

                InlineKeyboardMarkup markup = markupService.buildMarkupForUsers(users, "show_gear_");

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

            } else if ("Назад".equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.NONE);
                messageService.sendMessageWithKeyboard(chatId, "Возвращаю на главную", markupService.buildReplyKeyboardMarkup(userId));
            } else if(("/reminder_request".equalsIgnoreCase(message) || "Состояние аккумуляторов".equalsIgnoreCase(message))){
                if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
                    messageService.sendMessage(chatId, "Только командир может просматривать состояние аккумуляторов");
                    return;
                }

                List<User> users = userService.getAllVisibleUsers();

                if(users.isEmpty()){
                    messageService.sendMessage(chatId, "Команда пуста");
                    return;
                }

                InlineKeyboardMarkup markup = markupService.buildMarkupForChargedUsers(users, "reminder_request_");

                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Состояние аккумуляторов:")
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                messageService.sendMessage(chatId, "Команда не распознана");
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
                InlineKeyboardMarkup markup = markupService.buildMarkupForGear(userGears, "toggle_gear_");

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
            if(data.startsWith("reminder_response_")){
                Long telegramId = Long.parseLong(data.replace("reminder_response_", ""));
                userService.toggleChargeStatus(telegramId);
                InlineKeyboardMarkup markup = markupService.buildMarkupForAgreement(userId, "reminder_response_");

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
            if(data.startsWith("show_gear_")){
                Long targetTelegramId = Long.parseLong(data.replace("show_gear_", ""));
                List<Gear> userGears = gearService.getUserGears(targetTelegramId);

                if(!userService.isAdmin(userId) && !userService.isCommander(userId)){
                    messageService.sendMessage(chatId, "Нет прав для просмотра чужого снаряжения");
                    return;
                }

                if(userGears.isEmpty()){
                    messageService.sendMessage(chatId, "Пользователь пока не добавил снаряжение");
                    return;
                }

                InlineKeyboardMarkup markup = markupService.buildMarkupForGear(userGears, "toggle_gear_");

                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Снаряжение пользователя:")
                        .replyMarkup(markup)
                        .build();

                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            if(data.startsWith("delete_gear_")){
                Long gearId = Long.parseLong(data.replace("delete_gear_", ""));
                gearService.removeGear(userId, gearId);

                List<Gear> userGears = gearService.getUserGears(userId);

                if(userGears.isEmpty()){
                    messageService.sendMessage(chatId, "Все предметы удалены");
                    return;
                }

                InlineKeyboardMarkup updatedMarkup = markupService.buildMarkupForGearDeletion(userGears, "delete_gear_");

                EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(updatedMarkup)
                        .build();

                try {
                    telegramClient.execute(editMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void register(Long userId, String message, Long chatId) {
        userService.createUser(userId, message);
        userStates.put(userId, UserState.NONE);
        messageService.sendMessageWithKeyboard(chatId, "Добро пожаловать, %s!".formatted(message),
                markupService.buildReplyKeyboardMarkup(userId));
    }

    private void sendStartMessage(Long userId, Long chatId) {
        userStates.put(userId, UserState.ENTERING_CALLNAME);
        messageService.sendMessageHideKeyboard(chatId, """
            Привет! Введи свой позывной, чтобы начать работу.
            """);
    }

    private void sendWelcome(Long chatId, Long userId) {
        messageService.sendMessageWithKeyboard(chatId, "Добро пожаловать %s!"
                        .formatted(userService.findById(userId).get().getCallName()),
                markupService.buildReplyKeyboardMarkup(userId));
    }
}
