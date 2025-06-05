package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.enums.Role;
import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.UserRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private final Long TELEGRAM_ID = 1L;

    @Mock
    UserRepository repository;

    @InjectMocks
    UserServiceImpl service;

    // findById

    @Test
    void shouldFindUserSuccessfully() {
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        when(repository.findById(TELEGRAM_ID)).thenReturn(Optional.of(new User()));

        Optional<User> result = service.findById(TELEGRAM_ID);

        verify(repository).findById(captor.capture());

        assertTrue(result.isPresent());
        assertThat(captor.getValue()).isEqualTo(TELEGRAM_ID);

    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        when(repository.findById(TELEGRAM_ID)).thenReturn(Optional.empty());

        Optional<User> result = service.findById(TELEGRAM_ID);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    void shouldThrowNPEWhenTelegramIdIsNull() {
        doThrow(NullPointerException.class).when(repository).findById(null);

        assertThrows(NullPointerException.class, () -> service.findById(null));
    }

    // isAdmin

    @Test
    void shouldReturnTrueIfUserIsAdmin(){
        User user = new User(TELEGRAM_ID, "Тигр", false, Role.ADMIN, new ArrayList<>());
        when(repository.findById(TELEGRAM_ID)).thenReturn(Optional.of(user));

        boolean result = service.isAdmin(TELEGRAM_ID);

        verify(repository).findById(TELEGRAM_ID);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("prepareUsers1")
    void shouldReturnFalseIfUserIsNotAdmin(User user){
        when(repository.findById(user.getTelegramId())).thenReturn(Optional.of(user));

        boolean result = service.isAdmin(user.getTelegramId());

        verify(repository).findById(user.getTelegramId());

        assertThat(result).isFalse();
    }

    static Stream<User> prepareUsers1(){
        return Stream.of(
                new User(1L, "Тигр", false, Role.USER, new ArrayList<>()),
                new User(2L, "Кортеж", false, Role.COMMANDER, new ArrayList<>())
        );
    }

    // isCommander

    @Test
    void shouldReturnTrueIfUserIsCommander(){
        User user = new User(TELEGRAM_ID, "Тигр", false, Role.COMMANDER, new ArrayList<>());
        when(repository.findById(TELEGRAM_ID)).thenReturn(Optional.of(user));

        boolean result = service.isCommander(TELEGRAM_ID);

        verify(repository).findById(TELEGRAM_ID);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("prepareUsers3")
    void shouldReturnFalseIfUserIsNotCommander(User user){
        when(repository.findById(user.getTelegramId())).thenReturn(Optional.of(user));

        boolean result = service.isCommander(user.getTelegramId());

        verify(repository).findById(user.getTelegramId());

        assertThat(result).isFalse();
    }

    static Stream<User> prepareUsers3(){
        return Stream.of(
                new User(1L, "Тигр", false, Role.USER, new ArrayList<>()),
                new User(2L, "Кортеж", false, Role.ADMIN, new ArrayList<>())
        );
    }

    //проверка на null не нужна, т.к выше проверили уже findById

    // promoteToCommander

    @Test
    void shouldPromoteSuccessfully() {
        String callName = "Тигр";
        User user = new User(TELEGRAM_ID, callName, false, Role.USER, new ArrayList<>());

        when(repository.findAll()).thenReturn(List.of(user));

        service.promoteToCommander(callName);

        verify(repository).findAll();
        verify(repository).save(any());

        assertThat(user.getRole()).isEqualTo(Role.COMMANDER);
    }

    @Test
    void shouldThrowExceptionIfUserIsNotExists(){
        when(repository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->service.promoteToCommander("<UNK>"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(repository).findAll();
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNPEIfTelegramIdIsNull(){
        User user = new User(TELEGRAM_ID, null, false, Role.USER, new ArrayList<>());

        when(repository.findAll()).thenReturn(List.of(user));

        assertThatThrownBy(() ->service.promoteToCommander("<UNK>"))
            .isInstanceOf(NullPointerException.class);

        verify(repository).findAll();
        verify(repository, never()).save(any());
    }

    // getAllVisibleUsers

    @Test
    void shouldReturnUserListSuccessfully() {
        User user = new User(TELEGRAM_ID, "<UNK>", false, Role.USER, new ArrayList<>());
        User user1 = new User(TELEGRAM_ID, "<UNK>", false, Role.USER, new ArrayList<>());
        List<User> list = List.of(user, user1);

        when(repository.findAll()).thenReturn(list);

        List<User> result = service.getAllVisibleUsers();

        verify(repository).findAll();

        assertThat(result).isEqualTo(list);
    }

    @ParameterizedTest
    @MethodSource("prepareUsers2")
    void shouldReturnAllUsersExcludeUserWithAdminRole(List<User> users){
        when(repository.findAll()).thenReturn(users);

        List<User> result = service.getAllVisibleUsers();

        verify(repository).findAll();

        assertThat(result)
                .allSatisfy(user -> assertThat(user.getRole()).isNotEqualTo(Role.ADMIN));
    }

    static Stream<List<User>> prepareUsers2(){
        return Stream.of(
                List.of(
                        new User(1L, "Тигр", false, Role.USER, new ArrayList<>()),
                        new User(2L, "Кортеж", false, Role.COMMANDER, new ArrayList<>()),
                        new User(3L, "Бард", false, Role.ADMIN, new ArrayList<>())
                )
        );
    }

    @Test
    void shouldReturnEmptyListIfUsersDoesNotExist(){
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = service.getAllVisibleUsers();

        verify(repository).findAll();

        assertThat(result).isEmpty();
    }





}