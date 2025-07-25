package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
    Flux<Cart> findAllByUserId(Long userId);

    @Query("SELECT * FROM carts WHERE user_id = :userId AND item_id = :itemId")
    Mono<Cart> findByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);
    Mono<Void> deleteByUserId(Long userId);

}
