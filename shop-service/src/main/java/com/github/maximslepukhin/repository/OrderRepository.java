package com.github.maximslepukhin.repository;


import com.github.maximslepukhin.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAll();

    Flux<Order> findByUserId(Long userId);
}
