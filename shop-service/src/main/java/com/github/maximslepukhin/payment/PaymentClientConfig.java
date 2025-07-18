package com.github.maximslepukhin.payment;

import org.openapitools.client.ApiClient;
import org.openapitools.client.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {
    @Value("${payment-api.url}")
    private String paymentApiUrl;

    @Bean
    public ApiClient paymentApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(paymentApiUrl);
        return client;
    }

    @Bean
    public DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
