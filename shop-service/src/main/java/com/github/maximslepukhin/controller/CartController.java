package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.dto.ActionForm;
import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.enums.SortType;
import com.github.maximslepukhin.model.SecurityUser;
import com.github.maximslepukhin.paymentapi.PaymentApi;
import com.github.maximslepukhin.service.CartService;
import com.github.maximslepukhin.service.ItemService;
import com.github.maximslepukhin.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.*;

@Controller
@Slf4j
public class CartController {

    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentApi paymentApi;

    public CartController(ItemService itemService, OrderService orderService, CartService cartService, PaymentApi paymentApi) {
        this.itemService = itemService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentApi = paymentApi;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/main/items/{id}")
    public Mono<String> updateItemFromMain(@PathVariable Long id,
                                           @ModelAttribute ActionForm actionForm,
                                           @RequestParam(defaultValue = "") String search,
                                           @RequestParam(defaultValue = "NO") SortType sort,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(defaultValue = "1") int pageNumber,
                                           @AuthenticationPrincipal SecurityUser securityUser) {

        if (securityUser == null) {
            return Mono.just("redirect:/login");
        }
        Long userId = securityUser.getId();
        ActionType action = actionForm.getAction();
        return updateCart(userId, action, id)
                .thenReturn("redirect:/main/items?search=%s&sort=%s&pageSize=%s&pageNumber=%s"
                        .formatted(search, sort, pageSize, pageNumber));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/items/{id}")
    public Mono<String> updateItemFromCart(@PathVariable Long id,
                                           @ModelAttribute ActionForm actionForm,
                                           @AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            return Mono.just("redirect:/login");
        }
        Long userId = securityUser.getId();
        ActionType action = actionForm.getAction();
        return updateCart(userId, action, id)
                .thenReturn("redirect:/cart/items");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items/{id}")
    public Mono<String> updateItemFromItemPage(@PathVariable Long id,
                                               @ModelAttribute ActionForm actionForm,
                                               @AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            return Mono.just("redirect:/login");
        }
        Long userId = securityUser.getId();
        ActionType action = actionForm.getAction();
        return updateCart(userId, action, id)
                .thenReturn("redirect:/items/" + id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart/items")
    public Mono<String> showCart(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Mono<Map<Long, Integer>> cartMono;

        if (securityUser != null) {
            Long userId = securityUser.getId();
            cartMono = cartService.getUserCartMap(userId).defaultIfEmpty(new HashMap<>());
        } else {
            cartMono = Mono.just(new HashMap<>());
        }

        return cartMono.flatMap(cart ->
                itemService.getItemWithCount(cart)
                        .collectList()
                        .flatMap(itemsWithCount -> {
                            double total = itemsWithCount.stream()
                                    .mapToDouble(i -> i.getPrice() * i.getCount())
                                    .sum();

                            return paymentApi.balanceGet(securityUser.getId()).map(balance -> {
                                boolean showBuyButton = balance > total && !itemsWithCount.isEmpty();

                                model.addAttribute("total", total);
                                model.addAttribute("items", itemsWithCount);
                                model.addAttribute("showBuyButton", showBuyButton);
                                return "cart";
                            });
                        })
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/buy")
    public Mono<String> buyItemsFromCart(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getId();
        return cartService.getUserCartMap(userId)
                .flatMap(cart -> {
                    if (cart.isEmpty()) {
                        return Mono.just("redirect:/buy/error");
                    }

                    return itemService.getItemWithCount(cart)
                            .collectList()
                            .flatMap(itemsWithCount -> {
                                double totalAmount = itemsWithCount.stream()
                                        .mapToDouble(i -> i.getPrice() * i.getCount())
                                        .sum();

                                return paymentApi.balanceGet(userId)
                                        .flatMap(balance -> {
                                            if (balance == null || balance < totalAmount) {
                                                return Mono.just("redirect:/buy/error");
                                            }

                                            var paymentRequest = new org.openapitools.client.model.PaymentRequest();
                                            paymentRequest.setAmount(totalAmount);

                                            return paymentApi.paymentPost(userId, paymentRequest)
                                                    .flatMap(paymentResponse -> {
                                                        if (paymentResponse != null && Boolean.TRUE.equals(paymentResponse.getSuccess())) {
                                                            return orderService.createOrderFromCart(userId)
                                                                    .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
                                                        } else {
                                                            return Mono.just("redirect:/buy/error");
                                                        }
                                                    });
                                        });
                            });
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("redirect:/buy/error");
                });
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buy/error")
    public String showBuyErrorPage() {
        return "buy-error"; // шаблон buy-error.html
    }

    private Mono<Void> updateCart(Long userId, ActionType action, Long itemId) {
        if (action == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ActionType must not be null"));
        }
        return cartService.changeCart(userId, action, itemId)
                .onErrorResume(e -> {
                    System.err.println("Ошибка при обновлении корзины: " + e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}


