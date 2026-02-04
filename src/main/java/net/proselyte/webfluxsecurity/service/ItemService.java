package net.proselyte.webfluxsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.webfluxsecurity.dto.ItemRequest;
import net.proselyte.webfluxsecurity.entity.ItemEntity;
import net.proselyte.webfluxsecurity.repository.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;

    public Mono<ItemEntity> save(ItemRequest itemRequest, Long userId) {
        ItemEntity item = ItemEntity.builder()
                .userId(userId)
                .text(itemRequest.getText())
                .build();
        return itemRepository.save(item);
    }

    public Flux<ItemEntity> findByUserId(Long userId) {
        return itemRepository.findByUserId(userId).doOnNext(itemEntity -> {
            log.info("find item by id: {}", itemEntity);
        });
    }

    public Mono<ItemEntity> update(Long id, String newText, Long userId) {
        return itemRepository.findById(id)
                .filter(item -> item.getUserId().equals(userId))
                .flatMap(item -> {
                    item.setText(newText);
                    return itemRepository.save(item);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Запись не найдена или нет доступа")));
    }

    public Mono<Void> delete(Long id, Long userId) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Запись не найдена или нет доступа")))
                .filter(item -> item.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new RuntimeException("Нет доступа")))
                .flatMap(item -> itemRepository.deleteById(id));

    }

}
