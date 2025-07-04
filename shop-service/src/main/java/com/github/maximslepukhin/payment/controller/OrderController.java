package com.github.maximslepukhin.payment.controller;

import com.github.maximslepukhin.payment.dto.OrderWithItems;

import com.github.maximslepukhin.payment.service.*;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/orders")
    public Mono<String> showOrders(Model model) {
        Flux<OrderWithItems> ordersFlux = orderService.getAllOrdersWithItems();
        return ordersFlux.collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

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

