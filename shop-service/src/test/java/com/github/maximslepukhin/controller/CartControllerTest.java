package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.model.SecurityUser;
import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.paymentapi.PaymentApi;

import com.github.maximslepukhin.repository.ItemRepository;
import com.github.maximslepukhin.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureWebTestClient
public class CartControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PaymentApi paymentApi;

    private Item testItem;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll().block();
        testItem = itemRepository.save(
                new Item(null, "Ноутбук Lenovo", "15.6\", 16 ГБ RAM, SSD 512 ГБ", 80000, "images/lenovo.jpeg")
        ).block();
    }

    @AfterEach
    void cleanupUser() {
        userRepository.findByUsername("testUser")
                .flatMap(userRepository::delete)
                .block();
    }

    @Test
    void testShowCartEmpty() {
        Mockito.when(paymentApi.balanceGet(any()))
                .thenReturn(Mono.just(1000.0));
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                securityUser,
                                null,
                                List.of() // можешь добавить new SimpleGrantedAuthority("ROLE_USER")
                        )
                ))
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertThat(body).contains("Корзина товаров");
                    assertThat(body).contains("Итого: 0.0 руб.");
                    assertThat(body).doesNotContain("<button>Купить</button>");
                });
    }

    @Test
    void testUpdateItemFromMain() {
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, List.of())
                ))
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items/{id}")
                        .queryParam("search", "lenovo")
                        .queryParam("sort", "NO")
                        .queryParam("pageSize", 5)
                        .queryParam("pageNumber", 2)
                        .build(testItem.getId()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", ActionType.minus.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/main/items\\?search=lenovo&sort=NO&pageSize=5&pageNumber=2");
    }

    @Test
    void testUpdateItemFromCart() {
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, List.of())
                ))
                .post()
                .uri("/cart/items/" + testItem.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", ActionType.minus.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");
    }

    @Test
    void testUpdateItemFromItemPage() {
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, List.of())
                ))
                .post()
                .uri("/items/" + testItem.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", ActionType.plus.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/" + testItem.getId());
    }

    @Test
    void testBuyItemEmptyCart() {
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, List.of())
                ))
                .post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/buy/error");
    }

    @Test
    void testBuyErrorPageLoads() {
        SecurityUser securityUser = createTestSecurityUser();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, List.of())
                ))                .get()
                .uri("/buy/error")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("Ошибка при оформлении покупки");
                });
    }

    private SecurityUser createTestSecurityUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole("ROLE_USER");
        user.setBalance(300000.0);
        return new SecurityUser(user);
    }
}

