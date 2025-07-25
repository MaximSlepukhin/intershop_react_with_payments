package com.github.maximslepukhin.service;

import java.util.Map;

import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.model.Cart;
import com.github.maximslepukhin.repository.CartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Mono<Map<Long, Integer>> getUserCartMap(Long userId) {
        return cartRepository.findAllByUserId(userId)
                .collectMap(Cart::getItemId, Cart::getCount)
                .defaultIfEmpty(new HashMap<>());
    }

    public Mono<Void> changeCart(Long userId, ActionType action, Long itemId) {
        return cartRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(cartEntry -> {
                    int count = cartEntry.getCount();
                    switch (action) {
                        case plus:
                            cartEntry.setCount(count + 1);
                            return cartRepository.save(cartEntry).then();
                        case minus:
                            if (count > 1) {
                                cartEntry.setCount(count - 1);
                                return cartRepository.save(cartEntry).then();
                            } else {
                                return cartRepository.delete(cartEntry);
                            }
                        case delete:
                            return cartRepository.delete(cartEntry);
                        default:
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action"));
                    }
                })
                .switchIfEmpty(
                        action == ActionType.plus
                                ? cartRepository.save(new Cart(null, itemId, userId, 1)).then()
                                : Mono.empty()
                );
    }

    @Override
    public Mono<Void> clearCart(Long userId) {
        return cartRepository.deleteByUserId(userId);
    }
}
