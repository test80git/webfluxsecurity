package net.proselyte.webfluxsecurity.repository;

import net.proselyte.webfluxsecurity.entity.ItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveCrudRepository<ItemEntity, Long> {
    Flux<ItemEntity> findByUserId(Long userId);
}