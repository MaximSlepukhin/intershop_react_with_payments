package com.github.maximslepukhin.service;


import com.github.maximslepukhin.dto.OrderWithItems;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.model.Order;
import com.github.maximslepukhin.model.OrderItem;
import com.github.maximslepukhin.repository.ItemRepository;
import com.github.maximslepukhin.repository.OrderItemRepository;
import com.github.maximslepukhin.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private OrderItemRepository orderItemRepository;
    private CartService cartService;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        itemRepository = mock(ItemRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        cartService = mock(CartService.class);

        orderService = new OrderServiceImpl(orderRepository, itemRepository, orderItemRepository, cartService);
    }

    @Test
    void testCreateOrderFromCart_Success() {
        Long userId = 1L;
        Map<Long, Integer> cart = Map.of(1L, 2, 2L, 1);
        Item item1 = new Item(1L, "Item1", "", 100.0, "");
        Item item2 = new Item(2L, "Item2", "", 50.0, "");
        Order savedOrder = new Order(10L, 250.0, userId, null);

        when(cartService.getUserCartMap(userId)).thenReturn(Mono.just(cart));
        when(itemRepository.findAllById(cart.keySet())).thenReturn(Flux.just(item1, item2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(new OrderItem()));
        when(cartService.clearCart(userId)).thenReturn(Mono.empty());

        Long orderId = orderService.createOrderFromCart(userId).block();

        assertEquals(10L, orderId);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(cartService).clearCart(userId);
    }

    @Test
    void testCreateOrderFromCart_EmptyCart() {
        Long userId = 1L;
        when(cartService.getUserCartMap(userId)).thenReturn(Mono.just(Map.of()));

        assertThrows(IllegalStateException.class,
                () -> orderService.createOrderFromCart(userId).block());
    }

    @Test
    void testGetOrderWithItemsById_Success() {
        Long orderId = 1L;
        Order order = new Order(orderId, 100.0, 1L, null);
        OrderItem orderItem = new OrderItem(orderId, 1L, 2);
        Item item = new Item(1L, "Item1", "", 50.0, "");

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findAllById(List.of(1L))).thenReturn(Flux.just(item));

        OrderWithItems result = orderService.getOrderWithItemsById(orderId).block();

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("Item1", result.getItems().get(0).getTitle());
        assertEquals(2, result.getItems().get(0).getCount());
    }

    @Test
    void testGetAllOrdersWithItemsForUser_Success() {
        Long userId = 1L;
        Order order = new Order(1L, 100.0, userId, null);
        OrderItem orderItem = new OrderItem(1L, 2L, 1);
        Item item = new Item(2L, "Item2", "", 100.0, "");

        when(orderRepository.findByUserId(userId)).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findAllById(List.of(2L))).thenReturn(Flux.just(item));

        List<OrderWithItems> result = orderService.getAllOrdersWithItemsForUser(userId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item2", result.get(0).getItems().get(0).getTitle());
    }
}