package com.projectsky.blizzardbot.service;

import com.projectsky.blizzardbot.enums.Role;
import com.projectsky.blizzardbot.exception.GearAlreadyExistsException;
import com.projectsky.blizzardbot.exception.UserNotFoundException;
import com.projectsky.blizzardbot.model.Gear;
import com.projectsky.blizzardbot.model.User;
import com.projectsky.blizzardbot.repository.GearRepository;
import com.projectsky.blizzardbot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GearServiceImplTest {

    private final Long TELEGRAM_ID = 1L;
    private final Long GEAR_ID = 1L;
    private final String ITEM_NAME = "Шлем";

    @Captor
    ArgumentCaptor<Gear> captor;

    @Mock
    UserRepository userRepository;

    @Mock
    GearRepository gearRepository;

    @InjectMocks
    GearServiceImpl service;

    //addGear

    @Test
    void shouldAddGearSuccessfully() throws GearAlreadyExistsException {
        when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.of(new User()));
        when(gearRepository.existsByUserTelegramIdAndItemName(TELEGRAM_ID, ITEM_NAME)).thenReturn(false);

        service.addGear(TELEGRAM_ID, ITEM_NAME);

        verify(gearRepository).save(captor.capture());

        Gear captured = captor.getValue();

        assertThat(captured.getItemName()).isEqualTo(ITEM_NAME);
        assertThat(captured.isReady()).isFalse();
        assertThat(captured.getUser()).isNotNull();
    }

    @Test
    void shouldThrowGearExceptionIfGearExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(gearRepository.existsByUserTelegramIdAndItemName(TELEGRAM_ID, ITEM_NAME)).thenReturn(true);

        assertThatThrownBy(() -> service.addGear(TELEGRAM_ID, ITEM_NAME))
                .isInstanceOf(GearAlreadyExistsException.class)
                .hasMessageContaining("Gear already exists");

        verify(userRepository).findById(TELEGRAM_ID);
        verify(gearRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserNotFoundExceptionIfUserDoesNotExist(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addGear(TELEGRAM_ID, ITEM_NAME))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(gearRepository, never()).existsByUserTelegramIdAndItemName(TELEGRAM_ID, ITEM_NAME);
        verify(gearRepository, never()).save(any());
    }

    // toggleGearStatus

    @Test
    void shouldToggleGearStatusSuccessfully(){
        boolean startReadiness = false;
        Gear gear = new Gear(GEAR_ID, ITEM_NAME, startReadiness, new User(TELEGRAM_ID, "Тигр", false, Role.USER, new ArrayList<>()));

        when(gearRepository.findById(GEAR_ID)).thenReturn(Optional.of(gear));

        service.toggleGearStatus(TELEGRAM_ID, GEAR_ID);

        verify(gearRepository).findById(GEAR_ID);
        verify(gearRepository).save(captor.capture());

        Gear captured = captor.getValue();

        assertThat(captured).isEqualTo(gear);
        assertThat(captured.isReady()).isNotEqualTo(startReadiness);
    }

    @Test
    void shouldThrowExceptionIfGearDoesNotExist(){
        when(gearRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleGearStatus(TELEGRAM_ID, GEAR_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gear not found");

        verify(gearRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenUserIsNotTheSame(){
        Long differentUserId = 2L;
        Gear gear = new Gear(GEAR_ID, ITEM_NAME, false, new User(TELEGRAM_ID, "<UNK>", false, Role.USER, new ArrayList<>()));

        when(gearRepository.findById(GEAR_ID)).thenReturn(Optional.of(gear));

        assertThatThrownBy(() -> service.toggleGearStatus(differentUserId, GEAR_ID))
        .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not in telegram");

        verify(gearRepository).findById(GEAR_ID);
        verify(gearRepository, never()).save(any());
    }

    // isFullyEquipped

    @Test
    void shouldReturnTrueIfAllGearsReady(){
        User user = new User();
        Gear gear1 = new Gear(GEAR_ID, ITEM_NAME, true, user);
        Gear gear2 = new Gear(GEAR_ID + 1, ITEM_NAME + "other", true, user);


        when(gearRepository.findAllByUserTelegramId(TELEGRAM_ID)).thenReturn(List.of(gear1, gear2));

        boolean result = service.isFullyEquipped(TELEGRAM_ID);

        verify(gearRepository).findAllByUserTelegramId(TELEGRAM_ID);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseIfAllGearIsNotReady(){
        Gear gear = new Gear(GEAR_ID, ITEM_NAME, false, new User());

        when(gearRepository.findAllByUserTelegramId(TELEGRAM_ID)).thenReturn(List.of(gear));

        boolean result = service.isFullyEquipped(TELEGRAM_ID);

        verify(gearRepository).findAllByUserTelegramId(TELEGRAM_ID);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getGears")
    void shouldReturnFalseIfAtLeastOneGearIsNotReady(List<Gear> gears){
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        Long telegramId = gears.getFirst().getUser().getTelegramId();
        when(gearRepository.findAllByUserTelegramId(telegramId)).thenReturn(gears);

        boolean result = service.isFullyEquipped(telegramId);

        verify(gearRepository).findAllByUserTelegramId(captor.capture());

        assertThat(captor.getValue()).isEqualTo(gears.getFirst().getUser().getTelegramId());
        assertThat(result).isFalse();

    }

    @Test
    void shouldReturnFalseIfGearListIsEmpty(){
        when(gearRepository.findAllByUserTelegramId(TELEGRAM_ID)).thenReturn(Collections.emptyList());

        boolean result = service.isFullyEquipped(TELEGRAM_ID);

        verify(gearRepository).findAllByUserTelegramId(TELEGRAM_ID);
        assertThat(result).isFalse();
    }

    static Stream<List<Gear>> getGears(){
        User user = new User(1L, "Тигр", true, Role.USER, new ArrayList<>());
        return Stream.of(
                List.of(
                    new Gear(1L, "Шлем", false, user),
                    new Gear(2L, "Плитник", true, user),
                    new Gear(3L, "Варбелт", true, user)
                )
        );
    }

    // removeGear

    @Test
    void shouldRemoveGearSuccessfully(){
        Gear gear = new Gear();
        when(gearRepository.findByUserTelegramIdAndId(TELEGRAM_ID, GEAR_ID)).thenReturn(Optional.of(gear));
        doNothing().when(gearRepository).delete(gear);

        service.removeGear(TELEGRAM_ID, GEAR_ID);

        verify(gearRepository).findByUserTelegramIdAndId(TELEGRAM_ID, GEAR_ID);
        verify(gearRepository).delete(captor.capture());

        assertThat(captor.getValue()).isEqualTo(gear);
    }

    @Test
    void shouldThrowExceptionIfGearDoesNotExists(){
        when(gearRepository.findByUserTelegramIdAndId(TELEGRAM_ID, GEAR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeGear(TELEGRAM_ID, GEAR_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gear not found");

        verify(gearRepository).findByUserTelegramIdAndId(TELEGRAM_ID, GEAR_ID);
        verifyNoMoreInteractions(gearRepository);
    }

    @Test
    void shouldDoNotDeleteIfGearDoesNotRelateToUser(){
        User user = new User(1L, "<UNK>", true, Role.USER, new ArrayList<>());
        Gear gear = new Gear(GEAR_ID, ITEM_NAME, false, new User(2L, "<UNK>", false, Role.USER, new ArrayList<>()));

        when(gearRepository.findByUserTelegramIdAndId(user.getTelegramId(), gear.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeGear(user.getTelegramId(), gear.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gear not found");

        verify(gearRepository).findByUserTelegramIdAndId(user.getTelegramId(), gear.getId());
        verify(gearRepository, never()).delete(any());
    }

    @ParameterizedTest
    @MethodSource("nullParameters")
    void shouldThrowExceptionIfOneOfParameterIsNull(Long telegramId, Long gearId){
        assertThatThrownBy(() -> service.removeGear(telegramId, gearId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TelegramId and gearId must not be null");

        verifyNoInteractions(gearRepository);
    }

    static Stream<Arguments> nullParameters(){
        return Stream.of(
                Arguments.of(null, 1L),
                Arguments.of(1L, null),
                Arguments.of(null, null)
        );
    }

    // resetAllGearReadiness

    @Test
    void shouldSaveEmptyListIfGearListIsEmpty(){
        ArgumentCaptor<List<Gear>> listCaptor = ArgumentCaptor.forClass(List.class);
        when(gearRepository.findAll()).thenReturn(Collections.emptyList());

        service.resetAllGearReadiness();

        verify(gearRepository).findAll();
        verify(gearRepository).saveAll(listCaptor.capture());

        List<Gear> captured = listCaptor.getValue();

        assertThat(captured).isEmpty();
    }
    @ParameterizedTest
    @MethodSource("getGearsLists")
    void shouldResetAllSuccessfully_WhenAllGearsAreReady(List<Gear> gears){
        ArgumentCaptor<List<Gear>> listCaptor = ArgumentCaptor.forClass(List.class);
        when(gearRepository.findAll()).thenReturn(gears);

        service.resetAllGearReadiness();

        verify(gearRepository).findAll();
        verify(gearRepository).saveAll(listCaptor.capture());

        List<Gear> captured = listCaptor.getValue();

        assertThat(captured).hasSize(gears.size());
        assertThat(captured.stream().allMatch(Gear::isReady)).isFalse();
        assertThat(captured).allSatisfy(gear -> assertThat(gear.isReady()).isFalse());
    }

    static Stream<List<Gear>> getGearsLists(){
        return Stream.of(
                List.of(
                    new Gear(1L, "Шлем", true, new User()),
                    new Gear(2L, "Шлем", true, new User()),
                    new Gear(3L, "Шлем", true, new User())
                ),
                List.of(
                        new Gear(1L, "Шлем", false, new User()),
                        new Gear(2L, "Шлем", false, new User()),
                        new Gear(3L, "Шлем", false, new User())
                ),
                List.of(
                        new Gear(1L, "Шлем", true, new User()),
                        new Gear(2L, "Шлем", false, new User()),
                        new Gear(3L, "Шлем", true, new User())
                )
        );
    }

    // getUserGears
    @Test
    void shouldReturnSortedList(){
        User user = new User(1L, "e", true, Role.USER, new ArrayList<>());
        List<Gear> gears = new ArrayList<>();
        Gear gear = new Gear(1L, "b", true, user);
        Gear gear1 = new Gear(3L, "a", true, user);
        Gear gear2 = new Gear(2L, "c", true, user);
        gears.add(gear);
        gears.add(gear1);
        gears.add(gear2);


        when(gearRepository.findAllByUserTelegramId(user.getTelegramId())).thenReturn(gears);

        List<Gear> result = service.getUserGears(user.getTelegramId());

        assertThat(result)
                .extracting(Gear::getItemName)
                .isSorted();
    }

    @Test
    void shouldReturnEmptyListIfGearListIsEmpty(){
        when(gearRepository.findAllByUserTelegramId(TELEGRAM_ID)).thenReturn(Collections.emptyList());

        List<Gear> result = service.getUserGears(TELEGRAM_ID);

        assertThat(result).isEmpty();
    }

}