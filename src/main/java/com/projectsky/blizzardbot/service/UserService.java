package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.enums.Role;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<User> getAllVisibleUsers(){
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .toList();
    }

    public boolean isCommander(Long telegramId) {
        return findById(telegramId)
                .map(user -> user.getRole() == Role.COMMANDER)
                .orElse(false);
    }

    public Optional<User> findByCallName(String callName) {
        return userRepository.findByCallName(callName);
    }

    public boolean isAccCharged(Long telegramId) {
        return userRepository.findById(telegramId)
                .map(User::isAccumulatorCharged)
                .orElse(false);
    }

    public void toggleChargeStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccumulatorCharged(!user.isAccumulatorCharged());
        userRepository.save(user);
    }

    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }
}
