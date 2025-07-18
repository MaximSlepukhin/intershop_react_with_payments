package com.github.maximslepukhin.service;

import lombok.RequiredArgsConstructor;
import org.openapitools.api.DefaultApi;
import org.openapitools.model.PaymentRequest;
import org.openapitools.model.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class PaymentService implements DefaultApi {

    private static Double balance = 500000.0;

    @Override
    public Mono<ResponseEntity<Double>> _balanceGet(ServerWebExchange exchange) {
        synchronized (this) {
            return Mono.just(ResponseEntity.ok(balance));
        }
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentPost(Mono<PaymentRequest> paymentRequestMono, ServerWebExchange exchange) {
        return paymentRequestMono.map(request -> {
            double amountToPay = request.getAmount();
            PaymentResponse response = new PaymentResponse();

            synchronized (PaymentService.class) {
                if (balance >= amountToPay) {
                    balance -= amountToPay;
                    response.setSuccess(true);
                    response.setMessage("Платёж прошёл успешно. Остаток: " + balance);
                } else {
                    response.setSuccess(false);
                    response.setMessage("Недостаточно средств. Баланс: " + balance);
                }
            }

            return ResponseEntity.ok(response);
        });
    }
}
