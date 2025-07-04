package com.github.maximslepukhin.payment.intershop.service;


import com.github.maximslepukhin.payment.dto.OrderWithItems;
import com.github.maximslepukhin.payment.model.Item;
import com.github.maximslepukhin.payment.model.Order;
import com.github.maximslepukhin.payment.model.OrderItem;
import com.github.maximslepukhin.payment.repository.ItemRepository;
import com.github.maximslepukhin.payment.repository.OrderItemRepository;
import com.github.maximslepukhin.payment.repository.OrderRepository;
import com.github.maximslepukhin.payment.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private OrderItemRepository orderItemRepository;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        itemRepository = mock(ItemRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderService = new OrderServiceImpl(orderRepository, itemRepository, orderItemRepository);
    }

    @Test
    void createOrderFromCart_shouldCreateOrderAndSaveOrderItems() {
        Map<Long, Integer> cart = Map.of(1L, 2, 2L, 3);

        Item item1 = Item.builder()
                .id(1L)
                .price(10.0)
                .title("Item1")
                .description("Desc1")
                .imgPath("/img1.png")
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .price(20.0)
                .title("Item2")
                .description("Desc2")
                .imgPath("/img2.png")
                .build();

        when(itemRepository.findAllById(cart.keySet())).thenReturn(Flux.just(item1, item2));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return Mono.just(order);
        });

        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Long orderId = orderService.createOrderFromCart(cart).block();

        assertNotNull(orderId);
        assertEquals(100L, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        double expectedTotal = 10.0 * 2 + 20.0 * 3;
        assertEquals(expectedTotal, savedOrder.getTotalSum(), 0.001);

        ArgumentCaptor<OrderItem> orderItemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository, times(2)).save(orderItemCaptor.capture());

        List<OrderItem> savedOrderItems = orderItemCaptor.getAllValues();
        Map<Long, Integer> savedCounts = new HashMap<>();
        for (OrderItem oi : savedOrderItems) {
            assertEquals(100L, oi.getOrderId());
            savedCounts.put(oi.getItemId(), oi.getCount());
        }

        assertEquals(cart, savedCounts);
    }

    @Test
    void getAllOrdersWithItems_shouldReturnAllOrdersWithTheirItems() {
        Order order1 = new Order(1L, 100.0, null);
        Order order2 = new Order(2L, 200.0, null);

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));

        OrderItem oi1 = new OrderItem(1L, 10L, 5);
        OrderItem oi2 = new OrderItem(2L, 20L, 3);

        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(oi1));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Flux.just(oi2));

        Item item10 = Item.builder().id(10L).title("Item10").description("Desc10").price(100).imgPath("/img10").build();
        Item item20 = Item.builder().id(20L).title("Item20").description("Desc20").price(200).imgPath("/img20").build();

        when(itemRepository.findAllById(List.of(10L))).thenReturn(Flux.just(item10));
        when(itemRepository.findAllById(List.of(20L))).thenReturn(Flux.just(item20));

        List<OrderWithItems> result = orderService.getAllOrdersWithItems().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());

        OrderWithItems ow1 = result.stream().filter(o -> o.getOrderId().equals(1L)).findFirst().orElse(null);
        assertNotNull(ow1);
        assertEquals(1, ow1.getItems().size());
        assertEquals(item10.getId(), ow1.getItems().get(0).getId());
        assertEquals(5, ow1.getItems().get(0).getCount());

        OrderWithItems ow2 = result.stream().filter(o -> o.getOrderId().equals(2L)).findFirst().orElse(null);
        assertNotNull(ow2);
        assertEquals(1, ow2.getItems().size());
        assertEquals(item20.getId(), ow2.getItems().get(0).getId());
        assertEquals(3, ow2.getItems().get(0).getCount());
    }

    @Test
    void getOrderWithItemsById_shouldReturnOrderWithItems() {
        Order order = new Order(1L, 150.0, null);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));

        OrderItem orderItem = new OrderItem(1L, 5L, 7);

        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem));

        Item item = Item.builder().id(5L).title("Item5").description("Desc5").price(50).imgPath("/img5").build();

        when(itemRepository.findAllById(List.of(5L))).thenReturn(Flux.just(item));

        OrderWithItems result = orderService.getOrderWithItemsById(1L).block();

        assertNotNull(result);
        assertEquals(order.getId(), result.getOrderId());
        assertEquals(order.getTotalSum(), result.getTotalSum(), 0.001);
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().get(0).getId());
        assertEquals(7, result.getItems().get(0).getCount());
    }

    @Test
    void getOrderWithItemsById_shouldReturnNullWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Mono.empty());

        OrderWithItems result = orderService.getOrderWithItemsById(999L).block();

        assertNull(result);
    }
}