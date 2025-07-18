package com.github.maximslepukhin.payment;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.openapitools.client.model.PaymentRequest;
import org.openapitools.client.model.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentApiImpl implements PaymentApi {
    private final org.openapitools.client.api.DefaultApi paymentApi;


    @Override
    public Mono<Double> balanceGet() throws WebClientResponseException {
        return paymentApi.balanceGet();
    }

    @Override
    public Mono<PaymentResponse> paymentPost(@Nonnull PaymentRequest paymentRequest) throws WebClientResponseException {
        return paymentApi.paymentPost(paymentRequest);
    }
}
