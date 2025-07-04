package com.github.maximslepukhin.payment.service;

import com.github.maximslepukhin.payment.dto.OrderWithItems;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface OrderService {
    Mono<Long> createOrderFromCart(Map<Long, Integer> cart);

    Flux<OrderWithItems> getAllOrdersWithItems();

    Mono<OrderWithItems> getOrderWithItemsById(Long id);
}
