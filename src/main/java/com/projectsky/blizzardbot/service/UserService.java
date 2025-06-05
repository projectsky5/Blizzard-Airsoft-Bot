package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(Long telegramId, String callName);

    boolean isAdmin(Long telegramId);

    void promoteToCommander(String callName);

    List<User> getAllVisibleUsers();

    boolean isCommander(Long telegramId);

    Optional<User> findByCallName(String callName);

    boolean isAccCharged(Long telegramId);

    void toggleChargeStatus(Long userId);

    void saveAll(List<User> users);

    Optional<User> findById(Long userId);

    List<User> findAllWithGears();
}
