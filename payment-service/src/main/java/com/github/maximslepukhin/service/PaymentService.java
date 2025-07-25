package com.github.maximslepukhin.service;

import com.github.maximslepukhin.repository.UserRepository;
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

    private final UserRepository userRepository;

    @Override
    public Mono<ResponseEntity<Double>> balanceUserIdGet(Long userId, ServerWebExchange exchange) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user.getBalance()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentUserIdPost(Long userId, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(request ->
                userRepository.findById(userId)
                        .flatMap(user -> {
                            double currentBalance = user.getBalance();
                            double amountToPay = request.getAmount();
                            PaymentResponse response = new PaymentResponse();

                            if (currentBalance >= amountToPay) {
                                user.setBalance(currentBalance - amountToPay);
                                return userRepository.save(user)
                                        .map(updatedUser -> {
                                            response.setSuccess(true);
                                            response.setMessage("Платёж прошёл успешно. Остаток: " + updatedUser.getBalance());
                                            return ResponseEntity.ok(response);
                                        });
                            } else {
                                response.setSuccess(false);
                                response.setMessage("Недостаточно средств. Баланс: " + currentBalance);
                                return Mono.just(ResponseEntity.ok(response));
                            }
                        })
                        .switchIfEmpty(Mono.fromCallable(() -> {
                            PaymentResponse response = new PaymentResponse();
                            response.setSuccess(false);
                            response.setMessage("Пользователь не найден");
                            return ResponseEntity.ok(response);
                        }))
        );
    }
}
