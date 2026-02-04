package net.proselyte.webfluxsecurity.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.webfluxsecurity.api.ItemsApi;
import net.proselyte.webfluxsecurity.dto.ItemRequest;
import net.proselyte.webfluxsecurity.dto.ItemResponse;
import net.proselyte.webfluxsecurity.mapper.ItemMapper;
import net.proselyte.webfluxsecurity.security.CustomPrincipal;
import net.proselyte.webfluxsecurity.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/")
public class ItemsApiController implements ItemsApi {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @Override
    public Mono<ResponseEntity<ItemResponse>> createItem(
            Mono<ItemRequest> itemRequest,
            ServerWebExchange exchange) {

        log.info("OpenApi начало");
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();

                    return itemRequest.flatMap(req ->
                            itemService.save(req, principal.getId())
                                    .map(itemMapper::map)
                                    .map(response ->
                                            ResponseEntity.status(HttpStatus.CREATED).body(response)
                                    )
                    );
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteItem(Long id, ServerWebExchange exchange) {

        log.info("Начало удаления OpenAPI");
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(authentication -> {
                    CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

                    return itemService.delete(id, principal.getId())
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                            .onErrorResume(RuntimeException.class, e ->
                                    Mono.just(ResponseEntity.notFound().<Void>build()
                                    ));
                });
    }
}
