package com.projectsky.blizzardbot.util;

public class BotResponses {
    public static final String WELCOME = "Добро пожаловать, %s!";
    public static final String ENTER_CALLSIGN = "Привет! Введи свой позывной, чтобы начать работу.";
    public static final String START_GEAR_ADD = "Введите название предмета";
    public static final String GEAR_ADDED = "Предмет '%s' успешно добавлен.";
    public static final String GEAR_EXISTS = "Данный элемент снаряжения уже добавлен!";
    public static final String GEAR_EMPTY = "У тебя пока нет снаряжения";
    public static final String GEAR_REMOVED_ALL = "Все предметы удалены";
    public static final String GEAR_DELETE_PROMPT = "Выбери предмет для удаления:";
    public static final String ACTION_CHOICE = "Выберите действие";
    public static final String GEAR_LIST_USER = "Список вашего снаряжения:";
    public static final String GEAR_LIST_COMMANDER = "Список снаряжения пользователя:";
    public static final String COMMANDER_ONLY = "Только командир может просматривать %s";
    public static final String TEAM_EMPTY = "Команда пуста";
    public static final String UNKNOWN_COMMAND = "Команда не распознана";
    public static final String USER_PROMOTED = "Пользователь %s назначен командиром.";
    public static final String YOU_ARE_COMMANDER = "Ты был назначен командиром.";
    public static final String NO_ACCESS = "У тебя нет прав для назначения командира.";
    public static final String INVALID_COMMAND = "Использование: /set_commander @позывной";
    public static final String BACK_TO_MAIN = "Возвращаю на главную";
    public static final String NO_PERMISSION_VIEW_GEAR = "Нет прав для просмотра чужого снаряжения";
    public static final String USER_HAS_NO_GEAR = "Пользователь пока не добавил снаряжение";
    public static final String TEAM_STATUS = "Готовность команды:";
    public static final String BATTERY_STATUS = "Состояние аккумуляторов:";
    public static final String CANCEL_ADD = "Добавление отменено";
}
