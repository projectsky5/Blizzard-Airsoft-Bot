package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.model.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResetServiceImpl implements ResetService {

    private final UserService userService;
    private final GearService gearService;

    public ResetServiceImpl(UserService userService, GearService gearService) {
        this.userService = userService;
        this.gearService = gearService;
    }

    @Override
    @Scheduled(cron = "0 0 13 ? * MON", zone = "Europe/Moscow")
    @Transactional
    public void resetWeeklyStates() {
        List<User> users = userService.getAllVisibleUsers();
        if(!users.isEmpty()) {
            for (User user : users) {
                user.setAccumulatorCharged(false);
            }
            userService.saveAll(users);
        }

        gearService.resetAllGearReadiness();
    }
}
