package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.enums.Role;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    GearService gearService;

    @InjectMocks
    ResetServiceImpl service;

    @ParameterizedTest(name = "Reset for users")
    @MethodSource("prepareUsers")
    void shouldResetAllUsers(List<User> users) {
        ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        when(userService.getAllVisibleUsers()).thenReturn(users);

        service.resetWeeklyStates();

        verify(userService).getAllVisibleUsers();
        verify(userService).saveAll(captor.capture());
        verify(gearService).resetAllGearReadiness();

        List<User> captured = captor.getValue();

        assertThat(captured).isEqualTo(users);
        assertThat(captured).allSatisfy(user -> assertThat(user.isAccumulatorCharged()).isFalse());
    }

    @Test
    @DisplayName("Reset Only Gear Readiness When No Users")
    void shouldResetOnlyGearReadiness_WhenUserListIsEmpty() {
        when(userService.getAllVisibleUsers()).thenReturn(Collections.emptyList());

        service.resetWeeklyStates();

        verify(userService, never()).saveAll(any());
        verify(gearService).resetAllGearReadiness();
    }

    static Stream<List<User>> prepareUsers() {
        List<Gear> gears1 = new ArrayList<>();
        List<Gear> gears2 = new ArrayList<>();
        List<Gear> gears3 = new ArrayList<>();
        User user1 = new User(1L, "Тигр", false, Role.USER,gears1);
        gears1.add(new Gear(1L, "Шлем", true, user1));
        gears1.add(new Gear(2L, "Плитник", false, user1));
        gears1.add(new Gear(3L, "Варбелт", true, user1));

        User user2 = new User(2L, "Бард", false, Role.USER,gears2);
        gears2.add(new Gear(4L, "Шлем", true, user2));
        gears2.add(new Gear(5L, "Плитник", false, user2));
        gears2.add(new Gear(6L, "Варбелт", true, user2));

        User user3 = new User(3L, "Кортеж", true, Role.USER,gears3);
        gears3.add(new Gear(7L, "Шлем", true, user3));
        gears3.add(new Gear(8L, "Плитник", false, user3));
        gears3.add(new Gear(9L, "Варбелт", true, user3));

        return Stream.of(
                List.of(user1, user2, user3)
        );
    }

}