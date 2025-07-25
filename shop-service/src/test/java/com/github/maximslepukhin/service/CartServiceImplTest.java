package com.github.maximslepukhin.service;


import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.model.Cart;
import com.github.maximslepukhin.repository.CartRepository;
import com.github.maximslepukhin.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceImplTest {
    private CartRepository cartRepository;
    private CartServiceImpl cartService;

    private final Long userId = 1L;
    private final Long itemId = 100L;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartService = new CartServiceImpl(cartRepository);
    }

    @Test
    void getUserCartMap_shouldReturnMap() {
        Cart cart = new Cart(1L, itemId, userId, 2);
        when(cartRepository.findAllByUserId(userId)).thenReturn(Flux.just(cart));

        StepVerifier.create(cartService.getUserCartMap(userId))
                .expectNextMatches(map -> map.get(itemId) == 2)
                .verifyComplete();
    }

    @Test
    void changeCart_plus_shouldIncreaseCount() {
        Cart existing = new Cart(1L, itemId, userId, 2);
        when(cartRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(existing));
        when(cartRepository.save(any())).thenReturn(Mono.just(new Cart(1L, itemId, userId, 3)));

        StepVerifier.create(cartService.changeCart(userId, ActionType.plus, itemId))
                .verifyComplete();

        verify(cartRepository).save(argThat(cart -> cart.getCount() == 3));
    }

    @Test
    void changeCart_plus_shouldCreateNewIfNotExists() {
        when(cartRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(new Cart(1L, itemId, userId, 1)));

        StepVerifier.create(cartService.changeCart(userId, ActionType.plus, itemId))
                .verifyComplete();

        verify(cartRepository).save(argThat(cart -> cart.getCount() == 1));
    }

    @Test
    void changeCart_minus_shouldDecreaseCount() {
        Cart existing = new Cart(1L, itemId, userId, 3);
        when(cartRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(existing));
        when(cartRepository.save(any())).thenReturn(Mono.just(new Cart(1L, itemId, userId, 2)));

        StepVerifier.create(cartService.changeCart(userId, ActionType.minus, itemId))
                .verifyComplete();

        verify(cartRepository).save(argThat(cart -> cart.getCount() == 2));
    }

    @Test
    void changeCart_minus_shouldDeleteIfCountIsOne() {
        Cart existing = new Cart(1L, itemId, userId, 1);
        when(cartRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(existing));
        when(cartRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeCart(userId, ActionType.minus, itemId))
                .verifyComplete();

        verify(cartRepository).delete(existing);
    }

    @Test
    void changeCart_delete_shouldRemoveItem() {
        Cart existing = new Cart(1L, itemId, userId, 5);
        when(cartRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(existing));
        when(cartRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeCart(userId, ActionType.delete, itemId))
                .verifyComplete();

        verify(cartRepository).delete(existing);
    }

    @Test
    void clearCart_shouldCallRepository() {
        when(cartRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.clearCart(userId))
                .verifyComplete();

        verify(cartRepository).deleteByUserId(userId);
    }
}