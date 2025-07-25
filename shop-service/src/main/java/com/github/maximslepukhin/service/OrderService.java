package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.OrderWithItems;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface OrderService {

    Mono<Long> createOrderFromCart(Long userId);

    Mono<OrderWithItems> getOrderWithItemsById(Long id);

    Flux<OrderWithItems> getAllOrdersWithItemsForUser(Long userId);
}
