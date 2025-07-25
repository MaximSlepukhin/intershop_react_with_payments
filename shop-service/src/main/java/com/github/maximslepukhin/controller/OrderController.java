package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.dto.OrderWithItems;

import com.github.maximslepukhin.model.SecurityUser;
import com.github.maximslepukhin.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@org.springframework.stereotype.Controller
@Slf4j
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders")
    public Mono<String> showOrders(
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        Flux<OrderWithItems> ordersFlux = orderService.getAllOrdersWithItemsForUser(securityUser.getId());

        return ordersFlux.collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders/{id}")
    public Mono<String> showOrder(@PathVariable Long id,
                                  @RequestParam(defaultValue = "false") boolean newOrder,
                                  Model model) {
        return orderService.getOrderWithItemsById(id)
                .doOnNext(orderWithItems -> {
                    model.addAttribute("order", orderWithItems);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }
}

