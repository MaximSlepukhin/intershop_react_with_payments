package com.github.maximslepukhin.config;

import org.openapitools.client.ApiClient;
import org.openapitools.client.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
public class PaymentClientConfig {
    @Value("${payment-api.url}")
    private String paymentApiUrl;

    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public PaymentClientConfig(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean
    public WebClient webClient() {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                .baseUrl(paymentApiUrl)
                .filter(oauth2)
                .filter(logRequest())
                .build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient paymentWebClient) {
        ApiClient client = new ApiClient(paymentWebClient);
        return client.setBasePath(paymentApiUrl);
    }

    @Bean
    public DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }
}

