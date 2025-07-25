package com.github.maximslepukhin.service;


import com.github.maximslepukhin.dto.ItemWithCount;
import com.github.maximslepukhin.dto.OrderWithItems;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.model.Order;
import com.github.maximslepukhin.model.OrderItem;
import com.github.maximslepukhin.repository.ItemRepository;
import com.github.maximslepukhin.repository.OrderItemRepository;
import com.github.maximslepukhin.repository.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository,
                            OrderItemRepository orderItemRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
    }

    @Override
    public Mono<Long> createOrderFromCart(Long userId) {
        return cartService.getUserCartMap(userId)
                .flatMap(cart -> {
                    if (cart.isEmpty()) {
                        return Mono.error(new IllegalStateException("Cart is empty"));
                    }

                    Flux<Item> listOfItems = itemRepository.findAllById(cart.keySet());
                    return listOfItems
                            .collectList()
                            .flatMap(items -> {
                                double totalSum = items.stream()
                                        .mapToDouble(item -> item.getPrice() * cart.get(item.getId()))
                                        .sum();

                                Order order = new Order();
                                order.setTotalSum(totalSum);
                                order.setUserId(userId); // Важно

                                return orderRepository.save(order)
                                        .flatMap(o -> {
                                            List<OrderItem> orderItems = items.stream()
                                                    .map(i -> {
                                                        OrderItem orderItem = new OrderItem();
                                                        orderItem.setItemId(i.getId());
                                                        orderItem.setOrderId(o.getId());
                                                        orderItem.setCount(cart.get(i.getId()));
                                                        return orderItem;
                                                    })
                                                    .toList();

                                            return Flux.fromIterable(orderItems)
                                                    .flatMap(orderItemRepository::save)
                                                    .then(
                                                            cartService.clearCart(userId)
                                                                    .thenReturn(o.getId())
                                                    );
                                        });
                            });
                });
    }

    @Override
    public Mono<OrderWithItems> getOrderWithItemsById(Long id) {
        return orderRepository.findById(id)
                .flatMap(this::buildOrderWithItems);
    }

    @Override
    public Flux<OrderWithItems> getAllOrdersWithItemsForUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(this::buildOrderWithItems);
    }

    private Mono<OrderWithItems> buildOrderWithItems(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> {
                    List<Long> itemIds = orderItems.stream()
                            .map(OrderItem::getItemId)
                            .toList();

                    return itemRepository.findAllById(itemIds)
                            .collectList()
                            .map(items -> {
                                Map<Long, Integer> countMap = orderItems.stream()
                                        .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getCount));

                                List<ItemWithCount> itemsWithCounts = items.stream()
                                        .map(item -> new ItemWithCount(item, countMap.get(item.getId())))
                                        .toList();

                                return new OrderWithItems(order, itemsWithCounts);
                            });
                });
    }
}


