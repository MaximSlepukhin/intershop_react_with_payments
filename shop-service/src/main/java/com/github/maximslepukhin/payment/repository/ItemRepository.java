package com.github.maximslepukhin.payment.repository;


import com.github.maximslepukhin.payment.model.Item;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("title") String title,
            @Param("description") String description);

    Mono<Item> findById(Long id);

    Flux<Item> findAllById(Iterable<Long> ids);
}
