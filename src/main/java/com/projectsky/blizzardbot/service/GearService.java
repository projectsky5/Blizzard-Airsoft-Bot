package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.model.Gear;

import java.util.List;

public interface GearService {

    List<Gear> getUserGears(Long telegramId);
    void addGear(Long telegramId, String itemName) throws GearAlreadyExistsException;

    void toggleGearStatus(Long userId, Long gearId);

    Gear getGearById(Long gearId);
}
