package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.enums.ActionType;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.payment.PaymentApi;
import com.github.maximslepukhin.payment.PaymentApiImpl;
import com.github.maximslepukhin.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureWebTestClient
public class CartControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

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
        Mockito.when(paymentApi.balanceGet())
                        .thenReturn(Mono.just(1000.0));

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
