package com.github.maximslepukhin.payment.intershop.service;


import com.github.maximslepukhin.payment.enums.ActionType;
import com.github.maximslepukhin.payment.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class ItemServiceImplTest {
    private CartServiceImpl cartService;

    @BeforeEach
    void setup() {
        cartService = new CartServiceImpl();
    }

    @Test
    void testAddItemToEmptyCart() {
        Mono<Map<Long, Integer>> emptyCart = Mono.empty();

        Map<Long, Integer> result = cartService.changeCart(emptyCart, ActionType.plus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(1L));
    }

    @Test
    void testAddNewItemToExistingCart() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(2L, 3);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.plus, 1L).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(3, result.get(2L));
        assertEquals(1, result.get(1L));
    }

    @Test
    void testIncreaseCountOfExistingItem() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 2);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.plus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(1L));
    }

    @Test
    void testDecreaseCountWhenCountMoreThanOne() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 3);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.minus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(1L));
    }

    @Test
    void testRemoveItemWhenCountIsOne() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 1);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.minus, 1L).block();

        assertNotNull(result);
        assertFalse(result.containsKey(1L));
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteItem() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 5);
        existingCart.put(2L, 2);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.delete, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
    }

    @Test
    void testDeleteItemFromEmptyCart() {
        Mono<Map<Long, Integer>> emptyCart = Mono.empty();

        Map<Long, Integer> result = cartService.changeCart(emptyCart, ActionType.delete, 1L).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}