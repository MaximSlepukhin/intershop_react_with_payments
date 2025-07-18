package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
public class ItemControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private Item lenovoItem;
    private Item samsungItem;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll()
                .thenMany(itemRepository.saveAll(List.of(
                        new Item(null, "Ноутбук Lenovo", "15.6\", 16 ГБ RAM, SSD 512 ГБ", 80000, "images/lenovo.jpeg"),
                        new Item(null, "Смартфон Samsung", "6.5\" AMOLED, 128 ГБ памяти'", 90000, "images/samsung.jpeg")
                )))
                .collectList()
                .doOnNext(items -> {
                    lenovoItem = items.get(0);
                    samsungItem = items.get(1);
                })
                .block();
    }

    @Test
    void testRedirectToMain() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main/items");
    }

    @Test
    void testGetItemsPage() {
        webTestClient.get()
                .uri("/main/items?pageSize=5&pageNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).isNotNull();
                    assertThat(html).contains("Ноутбук Lenovo");
                    assertThat(html).contains("Смартфон Samsung");
                });
    }

    @Test
    void testSearchItems() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("search", "Lenovo")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).isNotNull();
                    assertThat(html).contains("Ноутбук Lenovo");
                    assertThat(html).doesNotContain("Смартфон Samsung");
                });
    }

    @Test
    void testGetItemById() {
        webTestClient.get()
                .uri("/items/" + lenovoItem.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).isNotNull();
                    assertThat(html).contains("Ноутбук Lenovo");
                    assertThat(html).contains("15.6&quot;, 16 ГБ RAM, SSD 512 ГБ");
                });
    }
}
