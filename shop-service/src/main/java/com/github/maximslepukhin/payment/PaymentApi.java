package com.github.maximslepukhin.payment;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public interface PaymentApi {
    Mono<Double> balanceGet() throws WebClientResponseException;
    Mono<org.openapitools.client.model.PaymentResponse> paymentPost(@jakarta.annotation.Nonnull org.openapitools.client.model.PaymentRequest paymentRequest) throws WebClientResponseException;
}
