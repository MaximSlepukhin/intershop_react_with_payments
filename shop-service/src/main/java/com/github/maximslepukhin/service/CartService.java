package com.github.maximslepukhin.service;


import com.github.maximslepukhin.enums.ActionType;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CartService {
    Mono<Map<Long, Integer>> changeCart(Mono<Map<Long, Integer>> cart, ActionType action, Long id);
}
