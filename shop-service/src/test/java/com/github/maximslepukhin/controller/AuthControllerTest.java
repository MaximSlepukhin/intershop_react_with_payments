package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@AutoConfigureWebTestClient
public class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll().block();
    }

    @Test
    void testShowRegistrationForm() {
        webTestClient.get()
                .uri("/register")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("<form");
                    assertThat(html).contains("username");
                    assertThat(html).contains("password");
                });
    }

    @Test
    void testRegisterNewUserAndRedirect() {
        String username = "newuser";
        String password = "securepass";

        webTestClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("username=" + username + "&password=" + password)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/login");
        User savedUser = userRepository.findByUsername(username).block();
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches(password, savedUser.getPassword())).isTrue();
        assertThat(savedUser.getBalance()).isEqualTo(300000.00);
    }

    @Test
    void testLoginFormWithoutError() {
        webTestClient.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("<form");
                    assertThat(html).doesNotContain("Ошибка входа");
                });
    }

    @Test
    void testLoginFormWithError() {
        String uriWithError = UriComponentsBuilder.fromPath("/login")
                .queryParam("error", "")
                .build()
                .toUriString();
        webTestClient.get()
                .uri(uriWithError)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assertThat(html).contains("<form");
                    assertThat(html).contains("login"); // шаблон должен отображать loginError через model
                });
    }
}