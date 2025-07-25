package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findById(Long userId);
}
