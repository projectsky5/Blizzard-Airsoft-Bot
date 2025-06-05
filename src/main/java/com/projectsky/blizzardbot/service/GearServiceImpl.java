package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.GearRepository;
import com.projectsky.blizzardbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GearServiceImpl implements GearService {

    private final GearRepository gearRepository;
    private final UserRepository userRepository;

    public GearServiceImpl(GearRepository gearRepository, UserRepository userRepository) {
        this.gearRepository = gearRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addGear(Long telegramId, String itemName) throws GearAlreadyExistsException {
        User user = userRepository.findById(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
    public boolean isFullyEquipped(Long telegramId) {
        List<Gear> gears = gearRepository.findAllByUserTelegramId(telegramId);
        return !gears.isEmpty() && gears.stream().allMatch(Gear::isReady);
    }

    @Override
    @Transactional
    public void removeGear(Long telegramId, Long gearId) {
        if(telegramId == null || gearId == null) {
            throw new IllegalArgumentException("TelegramId and gearId must not be null");
        }

        Optional<Gear> optGear = gearRepository.findByUserTelegramIdAndId(telegramId, gearId);
        if (optGear.isPresent()) {
            gearRepository.delete(optGear.get());
        } else {
            throw new RuntimeException("Gear not found");
        }
    }

    @Override
    @Transactional
    public void resetAllGearReadiness() {
        List<Gear> gears = gearRepository.findAll();
        gears.forEach(gear -> gear.setReady(false));
        gearRepository.saveAll(gears);
    }

    @Override
    public List<Gear> getUserGears(Long telegramId) {
        List<Gear> gears = gearRepository.findAllByUserTelegramId(telegramId);
        gears.sort(Comparator.comparing(Gear::getItemName));
        return gears;
    }
}
