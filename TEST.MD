# Тестирование
Главное: StepVerifier для тестирования Mono/Flux, when().thenReturn() для stub, verify() для mock проверок.

импорты import:
```java
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
```

Мокируем:  
```java
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository mockItemRepository;
}
```

Stub (задаём поведение):
```java
when(mockItemRepository.findById(1L))
    .thenReturn(Mono.just(existingItem));
```

Mock (проверяем вызовы):
```java
verify(mockItemRepository).findById(1L);
verify(mockItemRepository, never()).save(any());
```
Для реактивных потоков используй StepVerifier:
```java
StepVerifier.create(result)
    .expectNextMatches(item -> ...)
    .expectErrorMatches(throwable -> ...)
    .verifyComplete();
```
ArgumentMatchers:
```java
any(ItemEntity.class)  // любой объект ItemEntity
eq(1L)                 // строгое равенство
argThat(item -> ...)   // кастомная проверка
```
