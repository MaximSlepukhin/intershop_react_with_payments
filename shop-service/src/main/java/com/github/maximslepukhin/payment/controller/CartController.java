package com.github.maximslepukhin.payment.controller;



import com.github.maximslepukhin.payment.dto.ActionForm;
import com.github.maximslepukhin.payment.enums.ActionType;
import com.github.maximslepukhin.payment.enums.SortType;
import com.github.maximslepukhin.payment.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.*;

@Controller
@Slf4j
public class CartController {
    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;
    private static final String CART_SESSION_KEY = "cart";

    public CartController(ItemService itemService, OrderService orderService, CartService cartService) {
        this.itemService = itemService;
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> updateItemFromMain(@PathVariable Long id,
                                           @ModelAttribute ActionForm actionForm,
                                           @RequestParam(defaultValue = "") String search,
                                           @RequestParam(defaultValue = "NO") SortType sort,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(defaultValue = "1") int pageNumber,
                                           WebSession session) {
        return updateCart(session, actionForm.getAction(), id)
                .thenReturn("redirect:/main/items?search=%s&sort=%s&pageSize=%s&pageNumber=%s"
                        .formatted(search, sort, pageSize, pageNumber));
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> updateItemFromCart(@PathVariable Long id,
                                           @ModelAttribute ActionForm actionForm,
                                           WebSession session) {
        return updateCart(session, actionForm.getAction(), id)
                .thenReturn("redirect:/cart/items");
    }

    @PostMapping("/items/{id}")
    public Mono<String> updateItemFromItemPage(@PathVariable Long id,
                                               @ModelAttribute ActionForm actionForm,
                                               WebSession session) {
        return updateCart(session, actionForm.getAction(), id)
                .thenReturn("redirect:/items/" + id);
    }

    @GetMapping("/cart/items")
    public Mono<String> showCart(Model model, ServerWebExchange exchange) {

        return exchange.getSession()
                .flatMap(webSession -> {
                    Map<Long, Integer> cart = webSession.getAttribute("cart");
                    if (cart == null) {
                        cart = new HashMap<>();
                        webSession.getAttributes().put("cart", cart);
                    }
                    Map<Long, Integer> finalCart = cart;
                    return itemService.getItemWithCount(finalCart)
                            .collectList()
                            .map(itemsWithCount -> {
                                double total = itemsWithCount.stream()
                                        .mapToDouble(i -> i.getPrice() * i.getCount())
                                        .sum();
                                model.addAttribute("total", total);
                                model.addAttribute("items", itemsWithCount);
                                model.addAttribute("empty", itemsWithCount.isEmpty());
                                return "cart";
                            });
                });
    }

    @PostMapping("/buy")
    public Mono<String> buyItem(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    Map<Long, Integer> cart = session.getAttribute(CART_SESSION_KEY);
                    if (cart == null || cart.isEmpty()) {
                        return Mono.just("redirect:/main/items");
                    }

                    return orderService.createOrderFromCart(cart)
                            .map(orderId -> {
                                session.getAttributes().remove(CART_SESSION_KEY);
                                return "redirect:/orders/" + orderId + "?newOrder=true";
                            });
                });
    }

    private Mono<Void> updateCart(WebSession session, ActionType action, Long id) {
        if (action == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ActionType must not be null"));
        }
        Map<Long, Integer> cart = session.getAttribute(CART_SESSION_KEY);
        Mono<Map<Long, Integer>> cartMono = Mono.justOrEmpty(cart);

        return cartService.changeCart(cartMono, action, id)
                .doOnNext(updatedCart -> session.getAttributes().put(CART_SESSION_KEY, updatedCart))
                .then();
    }
}


