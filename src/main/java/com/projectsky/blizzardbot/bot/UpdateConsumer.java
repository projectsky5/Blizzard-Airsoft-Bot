package com.projectsky.blizzardbot.bot;

import com.projectsky.blizzardbot.configuration.BotProperties;
import com.projectsky.blizzardbot.service.UserService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashSet;
import java.util.Set;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;

    private final Set<Long> awaitingCallNames = new HashSet<>();

    public UpdateConsumer(BotProperties botProperties,
                          UserService userService) {
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
        this.userService = userService;
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
                    sendMessage(chatId, "Добро пожаловать %s!"
                                    .formatted(userService.findById(userId).get().getCallName())
                            );
                } else{
                    awaitingCallNames.add(userId);
                    sendMessage(chatId, """
                        Привет! Введи свой позывной, чтобы начать работу.
                        """);
                }
            }else if(!isRegistered && awaitingCallNames.contains(chatId)) {
                userService.createUser(userId, message);
                awaitingCallNames.remove(chatId);
                sendMessage(chatId, "Позывной '%s' сохранен! Добро пожаловать.".formatted(message));
                return;
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
}
