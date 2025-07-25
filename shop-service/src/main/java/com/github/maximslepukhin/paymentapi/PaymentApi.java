package com.github.maximslepukhin.paymentapi;

import jakarta.annotation.Nonnull;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public interface PaymentApi {
    Mono<Double> balanceGet(@Nonnull Long userId) throws WebClientResponseException;

    Mono<org.openapitools.client.model.PaymentResponse> paymentPost(@Nonnull Long userId,
                                                                    @Nonnull org.openapitools.client.model.PaymentRequest paymentRequest)
            throws WebClientResponseException;
}
