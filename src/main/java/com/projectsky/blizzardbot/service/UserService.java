package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.enums.Role;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(Long telegramId){
        return userRepository.findById(telegramId);
    }

    public User createUser(Long telegramId, String callName){
        User user = new User();
        user.setTelegramId(telegramId);
        user.setCallName(callName);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public boolean isAdmin(Long telegramId){
        return findById(telegramId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public void promoteToCommander(String callName){
        User user = userRepository.findAll().stream()
                .filter(u -> u.getCallName().equalsIgnoreCase(callName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.COMMANDER);
        userRepository.save(user);
    }
}
