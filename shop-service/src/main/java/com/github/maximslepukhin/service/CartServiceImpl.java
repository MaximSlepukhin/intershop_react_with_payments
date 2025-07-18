package com.github.maximslepukhin.service;

import java.util.Map;

import com.github.maximslepukhin.enums.ActionType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class CartServiceImpl implements CartService {

    @Override
    public Mono<Map<Long, Integer>> changeCart(Mono<Map<Long, Integer>> cartMono, ActionType action, Long id) {
        return cartMono.defaultIfEmpty(new HashMap<>())
                .map(cart -> {
                    Map<Long, Integer> newCart = new HashMap<>(cart);
                    switch (action) {
                        case plus -> newCart.put(id, newCart.getOrDefault(id, 0) + 1);
                        case minus -> {
                            int count = newCart.getOrDefault(id, 0);
                            if (count > 1) {
                                newCart.put(id, count - 1);
                            } else {
                                newCart.remove(id);
                            }
                        }
                        case delete -> newCart.remove(id);
                    }
                    return newCart;
                });
    }
}
