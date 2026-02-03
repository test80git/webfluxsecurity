package net.proselyte.webfluxsecurity.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.webfluxsecurity.entity.ItemEntity;
import net.proselyte.webfluxsecurity.security.CustomPrincipal;
import net.proselyte.webfluxsecurity.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/items")
public class ItemController {
    private static final Logger log = LoggerFactory.getLogger(AuthRestControllerV1.class);

    private final ItemService itemService;

    @PostMapping
    public Mono<ItemEntity> createItem(@RequestBody ItemEntity itemEntity,
                                       Authentication auth) {
        CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();
        log.info("Creating new item: {}", itemEntity);
        log.info("Principal name: {}, {}", principal.getName(), principal.getId());
        // principal.getId() - ID пользователя из токена
        itemEntity.setUserId(principal.getId());

        return itemService.save(itemEntity);
    }

    @GetMapping
    public Flux<ItemEntity> getMyItems(Authentication auth) {
        CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();
        return itemService.findByUserId(principal.getId());
    }

    // PUT для редактирования
    @PutMapping("/{id}")
    public Mono<ItemEntity> updateItem(
            @PathVariable Long id,
            @RequestBody ItemEntity itemUpdate,
            Authentication auth) {
        CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();
        return itemService.update(id, itemUpdate.getText(), principal.getId());
    }

    // DELETE для удаления
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteItem(
            @PathVariable Long id,
            Authentication auth) {
        CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();

        return itemService.delete(id, principal.getId())
                .then(Mono.just(ResponseEntity.ok().build()))
                .onErrorResume(RuntimeException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", e.getMessage()))))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Нет доступа"))));
    }

}
