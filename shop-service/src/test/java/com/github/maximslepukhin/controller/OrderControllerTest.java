package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.model.*;
import com.github.maximslepukhin.repository.ItemRepository;
import com.github.maximslepukhin.repository.OrderItemRepository;
import com.github.maximslepukhin.repository.OrderRepository;
import com.github.maximslepukhin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@AutoConfigureWebTestClient
public class OrderControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private SecurityUser securityUser;
    private Order savedOrder;
    private Item savedItem;

    @BeforeEach
    void setup() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        itemRepository.deleteAll().block();
        userRepository.deleteAll().block();

        User savedUser = userRepository.save(new User(null, "testUser", "password", "ROLE_USER", 300000.0)).block();
        assert savedUser != null;
        securityUser = new SecurityUser(savedUser);

        savedItem = itemRepository.save(new Item(
                null, "Ноутбук Lenovo", "15.6\", 16 ГБ RAM, SSD 512 ГБ", 80000, "images/lenovo.jpeg")).block();

        savedOrder = orderRepository.save(new Order(null, 160000.00, savedUser.getId(), null)).block();

        OrderItem orderItem = new OrderItem(savedOrder.getId(), savedItem.getId(), 2);
        orderItemRepository.save(orderItem).block();
    }

    @Test
    void testShowOrders() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                securityUser, null, List.of()
                        )
                ))
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("Ноутбук Lenovo");
                });
    }

    @Test
    void testShowOrderById() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                securityUser, null, List.of()
                        )
                ))
                .get()
                .uri("/orders/" + savedOrder.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("Заказ");
                    assertThat(html).contains("Ноутбук Lenovo");
                    assertThat(html).contains("2 шт.");
                });
    }

    @Test
    void testShowOrderByIdWithNewOrderFlag() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                securityUser, null, List.of()
                        )
                ))
                .get()
                .uri("/orders/" + savedOrder.getId() + "?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("Новый заказ");
                    assertThat(html).contains("Поздравляем! Успешная покупка!");
                });
    }
}