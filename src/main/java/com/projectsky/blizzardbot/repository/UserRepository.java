package com.projectsky.blizzardbot.repository;

import com.projectsky.blizzardbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCallName(String callName);

    @Query("""
    SELECT DISTINCT u
    FROM User u 
    JOIN FETCH u.gears
    """)
    List<User> findAllWithGears();
}
