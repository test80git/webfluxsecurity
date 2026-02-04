package net.proselyte.webfluxsecurity.service;

import net.proselyte.webfluxsecurity.entity.ItemEntity;
import net.proselyte.webfluxsecurity.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ItemServiceTest {

    @Mock
    private ItemRepository mockItemRepository;
    private ItemService itemService;
    private ItemEntity existingItem;
    private ItemEntity updatedItem;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(mockItemRepository);
        existingItem = ItemEntity.builder()
                .id(1L).userId(1L).text("Original Text")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        updatedItem = ItemEntity.builder()
                .id(1L).userId(1L).text("Updated Text")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveWithMock() {

        itemService.save(existingItem);
        verify(mockItemRepository).save(existingItem);
        verify(mockItemRepository, times(1)).save(existingItem);
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
    void findByUserIdWithMock() {
        itemService.findByUserId(2L);

        verify(mockItemRepository).findByUserId(2L);
        verify(mockItemRepository, times(1)).findByUserId(2L);
        verifyNoMoreInteractions(mockItemRepository);
    }

    @Test
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
    void update_shouldReturnError_whenUserIsNotOwner() {
        // Arrange
        ItemEntity otherUserItem = ItemEntity.builder()
                .id(1L)
                .userId(999L) // Другой пользователь
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
    void delete() {
    }
}