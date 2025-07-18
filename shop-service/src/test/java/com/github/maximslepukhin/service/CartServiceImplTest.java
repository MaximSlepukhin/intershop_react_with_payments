package com.github.maximslepukhin.service;


import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceImplTest {

    private CartServiceImpl cartService;

    @BeforeEach
    void setup() {
        cartService = new CartServiceImpl();
    }

    @Test
    void testAddItemWhenCartIsEmpty_block() {
        Mono<Map<Long, Integer>> emptyCart = Mono.empty();
        Map<Long, Integer> result = cartService.changeCart(emptyCart, ActionType.plus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(1L));
    }

    @Test
    void testAddItemWhenCartHasOtherItems_block() {
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
    void testIncreaseCountOfExistingItem_block() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 2);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.plus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(1L));
    }

    @Test
    void testDecreaseCountOfItemMoreThanOne_block() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 3);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.minus, 1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(1L));
    }

    @Test
    void testRemoveItemWhenCountIsOne_block() {
        Map<Long, Integer> existingCart = new HashMap<>();
        existingCart.put(1L, 1);
        Mono<Map<Long, Integer>> cartMono = Mono.just(existingCart);

        Map<Long, Integer> result = cartService.changeCart(cartMono, ActionType.minus, 1L).block();

        assertNotNull(result);
        assertFalse(result.containsKey(1L));
        assertEquals(0, result.size());
    }

    @Test
    void testDeleteItem_block() {
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
    void testDeleteItemWhenCartEmpty_block() {
        Mono<Map<Long, Integer>> emptyCart = Mono.empty();

        Map<Long, Integer> result = cartService.changeCart(emptyCart, ActionType.delete, 1L).block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

