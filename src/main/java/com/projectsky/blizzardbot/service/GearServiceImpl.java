package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.GearRepository;
import com.projectsky.blizzardbot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class GearServiceImpl implements GearService {

    private final GearRepository gearRepository;
    private final UserRepository userRepository;

    public GearServiceImpl(GearRepository gearRepository, UserRepository userRepository) {
        this.gearRepository = gearRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addGear(Long telegramId, String itemName) throws GearAlreadyExistsException {
        User user = userRepository.findById(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (gearRepository.existsByUserTelegramIdAndItemName(telegramId, itemName)) {
            throw new GearAlreadyExistsException("Gear already exists");
        }

        Gear gear = new Gear();
        gear.setItemName(itemName);
        gear.setUser(user);
        gear.setReady(false);

        gearRepository.save(gear);
    }

    @Override
    public void toggleGearStatus(Long userId, Long gearId) {
        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new RuntimeException("Gear not found"));
        if (!gear.getUser().getTelegramId().equals(userId)) {
            throw new RuntimeException("User not in telegram");
        }
        gear.setReady(!gear.isReady());
        gearRepository.save(gear);
    }

    @Override
    public Gear getGearById(Long gearId) {
        return gearRepository.findById(gearId)
                .orElseThrow(() -> new RuntimeException("Gear not found"));
    }

    @Override
    public boolean isFullyEquipped(Long telegramId) {
        List<Gear> gears = gearRepository.findAllByUserTelegramId(telegramId);
        return !gears.isEmpty() && gears.stream().allMatch(Gear::isReady);
    }

    @Override
    public List<Gear> getUserGears(Long telegramId) {
        List<Gear> gears = gearRepository.findAllByUserTelegramId(telegramId);
        gears.sort(Comparator.comparing(Gear::getItemName));
        return gears;
    }
}
