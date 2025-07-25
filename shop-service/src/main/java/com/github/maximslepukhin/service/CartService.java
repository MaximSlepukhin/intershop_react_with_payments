package com.github.maximslepukhin.service;


import com.github.maximslepukhin.enums.ActionType;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CartService {

    Mono<Void> changeCart(Long userId, ActionType action, Long id);

    Mono<Map<Long, Integer>> getUserCartMap(Long userId);

    Mono<Void> clearCart(Long userId);
}
