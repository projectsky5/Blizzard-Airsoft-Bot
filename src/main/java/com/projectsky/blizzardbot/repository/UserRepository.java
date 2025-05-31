package com.projectsky.blizzardbot.repository;

import com.projectsky.blizzardbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCallName(String callName);
}
