package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.enums.UserState;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.service.GearService;
import com.projectsky.blizzardbot.service.MarkupService;
import com.projectsky.blizzardbot.service.MessageService;
import com.projectsky.blizzardbot.service.UserServiceImpl;
import com.projectsky.blizzardbot.util.BotResponses;
import com.projectsky.blizzardbot.util.ButtonText;
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

    private final UserServiceImpl userService;
    private final GearService gearService;
    private final MessageService messageService;
    private final MarkupService markupService;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UpdateConsumer(UserServiceImpl userService,
                          GearService gearService,
                          MessageService messageService,
                          MarkupService markupService) {
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
            ReplyKeyboardMarkup mainKeyboard = markupService.buildReplyKeyboardMarkup(userId);

            boolean isRegistered = userService.findById(userId).isPresent();

            if("/start".equals(message)) {
                if(isRegistered) {
                    sendWelcome(chatId, userId);
                } else{
                    sendStartMessage(userId, chatId);
                }
            } else if(!isRegistered && userStates.getOrDefault(userId, UserState.NONE) == UserState.ENTERING_CALLNAME) {
                register(userId, message, chatId);

            } else if ("/add_gear".equalsIgnoreCase(message) || ButtonText.ADD_GEAR.equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.ADDING_GEAR);

                messageService.sendMessageWithCancelKeyboard(
                        chatId,
                        BotResponses.START_GEAR_ADD,
                        markupService.buildReplyKeyboardCancelMarkup(userId)
                );

            } else if(userStates.getOrDefault(userId, UserState.NONE) == UserState.ADDING_GEAR) {
                try {
                    if(ButtonText.CANCEL.equals(message)) {

                        messageService.sendMessageWithKeyboard(
                                chatId,
                                BotResponses.CANCEL_ADD,
                                mainKeyboard
                        );

                        userStates.put(userId, UserState.NONE);
                        return;
                    }

                    gearService.addGear(userId, message);
                } catch (GearAlreadyExistsException e) {

                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.GEAR_EXISTS,
                            mainKeyboard
                    );

                    userStates.put(userId, UserState.NONE);
                    return;
                }
                userStates.put(userId, UserState.GEAR_MODE);

                messageService.sendMessageWithKeyboard(
                        chatId,
                        BotResponses.GEAR_ADDED.formatted(message),
                        markupService.buildReplyKeyboardGearMode(userId)
                );
            } else if("/check_gear".equalsIgnoreCase(message) || ButtonText.MY_GEAR.equalsIgnoreCase(message)) {
                sendGearMenu(userId, chatId);

                messageService.sendMessageWithKeyboard(
                        chatId,
                        BotResponses.ACTION_CHOICE,
                        markupService.buildReplyKeyboardGearMode(userId)
                );

            } else if(ButtonText.VIEW_GEAR.equalsIgnoreCase(message)) {
                sendGearMenu(userId, chatId);
            }
            else if (ButtonText.REMOVE_GEAR.equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.REMOVING_GEAR);

                List<Gear> userGears = gearService.getUserGears(userId);

                if(userGears.isEmpty()){

                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.GEAR_EMPTY,
                            mainKeyboard
                    );
                    return;
                }

                messageService.sendMessageWithInlineKeyboard(
                        chatId,
                        BotResponses.GEAR_DELETE_PROMPT,
                        markupService.buildMarkupForGearDeletion(userGears, "delete_gear_")
                );

            } else if(message.startsWith("/set_commander")) {
                if (!userService.isAdmin(userId)) {

                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.NO_ACCESS,
                            mainKeyboard
                    );

                    return;
                }

                String[] parts = message.split(" ");
                if (parts.length != 2) {
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.INVALID_COMMAND,
                            mainKeyboard
                    );
                    return;
                }

                String callName = parts[1].replace("@", "");

                userService.promoteToCommander(callName);

                User promotedUser = userService.findByCallName(callName)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                messageService.sendMessageWithKeyboard(
                        chatId,
                        BotResponses.USER_PROMOTED.formatted(callName),
                        mainKeyboard
                );

                messageService.sendMessageWithKeyboard(
                        promotedUser.getTelegramId(),
                        BotResponses.YOU_ARE_COMMANDER,
                        mainKeyboard
                );

            } else if("/team_status".equalsIgnoreCase(message) || ButtonText.TEAM_STATUS.equalsIgnoreCase(message)) {
                if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.COMMANDER_ONLY.formatted(message),
                            mainKeyboard
                    );

                    return;
                }

                List<User> users = userService.getAllVisibleUsers();

                if(users.isEmpty()){
                    messageService.sendMessageWithKeyboard(chatId,
                            BotResponses.TEAM_EMPTY,
                            mainKeyboard);
                    return;
                }
                messageService.sendMessageWithInlineKeyboard(
                        chatId,
                        BotResponses.TEAM_STATUS,
                        markupService.buildMarkupForUsers(users, "show_gear_"));

            } else if (ButtonText.BACK.equalsIgnoreCase(message)) {
                userStates.put(userId, UserState.NONE);
                messageService.sendMessageWithKeyboard(
                        chatId,
                        BotResponses.BACK_TO_MAIN,
                        mainKeyboard
                );
            } else if(("/reminder_request".equalsIgnoreCase(message) || ButtonText.ACCUMULATOR_STATUS.equalsIgnoreCase(message))){
                if (!userService.isCommander(userId) && !userService.isAdmin(userId)) {
                    messageService.sendMessageWithKeyboard(chatId,
                            BotResponses.COMMANDER_ONLY.formatted(message),
                            mainKeyboard);
                    return;
                }

                List<User> users = userService.getAllVisibleUsers();

                if(users.isEmpty()){
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.TEAM_EMPTY,
                            mainKeyboard
                    );

                    return;
                }

                messageService.sendMessageWithInlineKeyboard(
                        chatId,
                        BotResponses.BATTERY_STATUS,
                        markupService.buildMarkupForChargedUsers(users, "reminder_request_"));
            }
            else {
                messageService.sendMessageWithKeyboard(
                        chatId,
                        BotResponses.UNKNOWN_COMMAND,
                        mainKeyboard
                );
            }
        }
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Long userId = callbackQuery.getFrom().getId();
            Long chatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            ReplyKeyboardMarkup mainKeyboard = markupService.buildReplyKeyboardMarkup(userId);


            if (data.startsWith("toggle_gear_")){
                Long gearId = Long.parseLong(data.replace("toggle_gear_", ""));
                gearService.toggleGearStatus(userId, gearId);

                List<Gear> userGears = gearService.getUserGears(userId);

                messageService.sendMessageWithEditKeyboard(
                        chatId,
                        messageId,
                        markupService.buildMarkupForGear(userGears, "toggle_gear_")
                );

            }
            if(data.startsWith("reminder_response_")){
                Long telegramId = Long.parseLong(data.replace("reminder_response_", ""));
                userService.toggleChargeStatus(telegramId);

                messageService.sendMessageWithEditKeyboard(
                        chatId,
                        messageId,
                        markupService.buildMarkupForAgreement(userId, "reminder_response_")
                );
            }
            if(data.startsWith("show_gear_")){
                Long targetTelegramId = Long.parseLong(data.replace("show_gear_", ""));
                List<Gear> userGears = gearService.getUserGears(targetTelegramId);

                if(!userService.isAdmin(userId) && !userService.isCommander(userId)){
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.NO_PERMISSION_VIEW_GEAR,
                            mainKeyboard
                    );
                    return;
                }

                if(userGears.isEmpty()){
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.USER_HAS_NO_GEAR,
                            mainKeyboard
                    );
                    return;
                }

                messageService.sendMessageWithInlineKeyboard(
                        chatId,
                        BotResponses.GEAR_LIST_COMMANDER,
                        markupService.buildMarkupForGear(userGears, "show_gear_")
                );
            }
            if(data.startsWith("delete_gear_")){
                Long gearId = Long.parseLong(data.replace("delete_gear_", ""));
                gearService.removeGear(userId, gearId);

                List<Gear> userGears = gearService.getUserGears(userId);

                if(userGears.isEmpty()){
                    messageService.sendMessageWithKeyboard(
                            chatId,
                            BotResponses.GEAR_REMOVED_ALL,
                            mainKeyboard
                    );
                    return;
                }

                messageService.sendMessageWithEditKeyboard(
                        chatId,
                        messageId,
                        markupService.buildMarkupForGearDeletion(userGears, "delete_gear_")
                );
            }
        }
    }

    private void sendGearMenu(Long userId, Long chatId) {
        List<Gear> userGears = gearService.getUserGears(userId);

        messageService.sendMessageWithInlineKeyboard(
                chatId,
                BotResponses.GEAR_LIST_USER,
                markupService.buildMarkupForGear(userGears, "toggle_gear_")
        );
    }

    private void register(Long userId, String message, Long chatId) {
        userService.createUser(userId, message);
        userStates.put(userId, UserState.NONE);
        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.WELCOME.formatted(message),
                markupService.buildReplyKeyboardMarkup(userId)
        );
    }

    private void sendStartMessage(Long userId, Long chatId) {
        userStates.put(userId, UserState.ENTERING_CALLNAME);
        messageService.sendMessageHideKeyboard(
                chatId,
                BotResponses.ENTER_CALLSIGN
        );
    }

    private void sendWelcome(Long chatId, Long userId) {
        messageService.sendMessageWithKeyboard(
                chatId,
                BotResponses.WELCOME.formatted(userService.findById(userId).get().getCallName()),
                markupService.buildReplyKeyboardMarkup(userId)
        );
    }
}
