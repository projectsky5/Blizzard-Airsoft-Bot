package com.projectsky.blizzardbot.repository;

import com.projectsky.blizzardbot.model.Gear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GearRepository extends JpaRepository<Gear, Long> {
    List<Gear> findAllByUserTelegramId(Long telegramId);

    void deleteByUserTelegramId_AndId(Long userTelegramId, Long gearId);

    boolean existsByUserTelegramIdAndItemName(Long userTelegramId, String itemName);
}
