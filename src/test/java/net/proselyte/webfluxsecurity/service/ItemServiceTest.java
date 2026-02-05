package net.proselyte.webfluxsecurity.service;


import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import net.proselyte.webfluxsecurity.dto.ItemRequest;
import net.proselyte.webfluxsecurity.entity.ItemEntity;
import net.proselyte.webfluxsecurity.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Item Management")
@Feature("Item Service")
class ItemServiceTest {

    @Mock
    private ItemRepository mockItemRepository;
    private ItemService itemService;
    private ItemEntity existingItem;
    private ItemEntity updatedItem;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(mockItemRepository);
        existingItem = ItemEntity.builder()
                .id(1L)
                .userId(1L)
                .text("Original Text")
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
                .build();

        updatedItem = ItemEntity.builder()
                .id(1L)
                .userId(1L)
                .text("Updated Text")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        itemRequest = new ItemRequest();
        itemRequest.setText("Text Request");
    }

    @Test
    @Tag("API")  
    @DisplayName("Save item with mock verification")
    @Description("Test saving item with mock verification")
    @Story("Save item functionality")
    void saveWithMock() {

        itemService.save(itemRequest, 1L);
        existingItem.setText(itemRequest.getText());

        verify(mockItemRepository).save(argThat(item ->
                item.getText().equals("Text Request") &&
                item.getUserId().equals(1L) &&
                item.getId() == null
        ));
        verify(mockItemRepository, times(1)).save(argThat(item ->
                item.getText().equals("Text Request") &&
                item.getUserId().equals(1L) &&
                item.getId() == null  // ID должен быть null при сохранении
        ));
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    @Tag("API")  
    @DisplayName("Save item with correct fields")
    @Description("Test that item is saved with correct fields")
    @Story("Save item functionality")
    void save_shouldSaveItemWithCorrectFields() {
        // Arrange (Stub)
        when(mockItemRepository.save(any(ItemEntity.class)))
                .thenReturn(Mono.just(existingItem));

        // Act
        Mono<ItemEntity> result = itemService.save(itemRequest, 1L);

        // Assert (StepVerifier)
        StepVerifier.create(result)
                .expectNextMatches(item ->
                        item.getId().equals(1L) &&
                        item.getText().equals("Original Text") &&
                        item.getUserId().equals(1L)
                )
                .verifyComplete();

        // Verify
        verify(mockItemRepository).save(argThat(item ->
                item.getText().equals("Text Request") &&
                item.getUserId().equals(1L) &&
                item.getId() == null
        ));
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    @Tag("API")  
    @DisplayName("Find items by user ID with stub")
    @Description("Test finding items by user ID using stub")
    @Story("Find items by user")
    void findByUserIdWithStub() {
        // Arrange - настраиваем mock
        Flux<ItemEntity> expectedFlux = Flux.just(
                new ItemEntity(1L,  2L, "Item1", LocalDateTime.now(), LocalDateTime.now()),
                new ItemEntity(2L,  2L, "Item2", LocalDateTime.now(), LocalDateTime.now())
        );

        when(mockItemRepository.findByUserId(2L)).thenReturn(expectedFlux);

        // Act
        Flux<ItemEntity> result = itemService.findByUserId(2L);

        // Assert
        verify(mockItemRepository).findByUserId(2L);
        verify(mockItemRepository, times(1)).findByUserId(2L);

        // Дополнительно можно проверить, что flux содержит элементы
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @Tag("API")  
    @DisplayName("Find items by user ID with mock")
    @Description("Test finding items by user ID using mock")
    @Story("Find items by user")
    void findByUserIdWithMock() {
        // Настраиваем mock чтобы не возвращал null
        when(mockItemRepository.findByUserId(2L)).thenReturn(Flux.empty());

        // Act
        itemService.findByUserId(2L);

        // Assert
        verify(mockItemRepository).findByUserId(2L);
        verify(mockItemRepository, times(1)).findByUserId(2L);
    }

    @Test
    @Tag("API")  
    void update_shouldUpdateItem_whenItemExistsAndUserIsOwner() {
        // Arrange (Stub - задаём поведение мока)
        when(mockItemRepository.findById(1L))
                .thenReturn(Mono.just(existingItem));

        when(mockItemRepository.save(any(ItemEntity.class)))
                .thenReturn(Mono.just(updatedItem));

        // Act
        Mono<ItemEntity> result = itemService.update(1L, "Updated Text", 1L);

        // Assert (StepVerifier для реактивных потоков)
        StepVerifier.create(result)
                .expectNextMatches(item ->
                        item.getId().equals(1L) &&
                        item.getText().equals("Updated Text") &&
                        item.getUserId().equals(1L)
                )
                .verifyComplete();

        // Verify (Mock - проверяем взаимодействия)
        verify(mockItemRepository).findById(1L);
        verify(mockItemRepository).save(argThat(item ->
                item.getText().equals("Updated Text") &&
                item.getId().equals(1L)
        ));
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    @Tag("API")  
    void update_shouldReturnError_whenItemNotFound() {
        // Arrange
        when(mockItemRepository.findById(1L))
                .thenReturn(Mono.empty());

        // Act
        Mono<ItemEntity> result = itemService.update(1L, "New Text", 1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Запись не найдена или нет доступа")
                )
                .verify();

        // Verify
        verify(mockItemRepository).findById(1L);
        verify(mockItemRepository, never()).save(any());
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    @Tag("API")
    void update_shouldReturnError_whenUserIsNotOwner() {
        // Arrange
        ItemEntity otherUserItem = ItemEntity.builder()
                .id(1L)
                .userId(999L)
                .text("Original Text")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(mockItemRepository.findById(1L))
                .thenReturn(Mono.just(otherUserItem));

        // Act
        Mono<ItemEntity> result = itemService.update(1L, "Updated Text", 1L); // userId=1 пытается изменить запись userId=999

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Запись не найдена или нет доступа")
                )
                .verify();

        // Verify
        verify(mockItemRepository).findById(1L);
        verify(mockItemRepository, never()).save(any());
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    @Tag("API")
    void update_shouldSaveWithUpdatedText() {
        // Arrange
        when(mockItemRepository.findById(1L))
                .thenReturn(Mono.just(existingItem));

        // Проверяем, что текст изменился перед сохранением
        when(mockItemRepository.save(argThat(item ->
                item.getText().equals("Updated Text")
        ))).thenReturn(Mono.just(updatedItem));

        // Act
        itemService.update(1L, "Updated Text", 1L).block();

        // Verify
        verify(mockItemRepository).save(argThat(item -> {
            assert item.getText().equals("Updated Text");
            assert item.getId().equals(1L);
            assert item.getUserId().equals(1L);
            return true;
        }));
    }

    @Test
    @Tag("API")
    void delete() {
    }
}