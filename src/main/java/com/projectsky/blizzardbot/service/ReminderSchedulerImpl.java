package com.projectsky.blizzardbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReminderSchedulerImpl implements ReminderScheduler {

    private final ReminderService reminderService;
    public ReminderSchedulerImpl(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
//    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Moscow")
    @Scheduled(fixedDelay = 10000)
    public void sendToAll() {
        reminderService.sendReminder();
    }
}
