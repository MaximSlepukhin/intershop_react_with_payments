package com.github.maximslepukhin.payment.intershop.controller;


import com.github.maximslepukhin.payment.enums.ActionType;
import com.github.maximslepukhin.payment.model.Item;
import com.github.maximslepukhin.payment.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;


import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
public class CartControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll().block();
        testItem = itemRepository.save(
                new Item(null, "Ноутбук Lenovo", "15.6\", 16 ГБ RAM, SSD 512 ГБ", 80000, "images/lenovo.jpeg")
        ).block();
    }

    @Test
    void testUpdateItemFromMain() {
        webTestClient.post()
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
        webTestClient.post()
                .uri("/cart/items/" + testItem.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", ActionType.minus.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");
    }

    @Test
    void testUpdateItemFromItemPage() {
        webTestClient.post()
                .uri("/items/" + testItem.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("action", ActionType.plus.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/" + testItem.getId());
    }

    @Test
    void testShowCartEmpty() {
        webTestClient.get()
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
    void testBuyItemEmptyCart() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main/items");
    }
}
