package com.github.maximslepukhin.repository;


import com.github.maximslepukhin.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    Mono<Item> findById(Long id);

    Flux<Item> findAllById(Iterable<Long> ids);

    @Query("""
                SELECT * FROM items 
                WHERE (title ILIKE '%' || :search || '%' 
                       OR description ILIKE '%' || :search || '%')
                ORDER BY title ASC
                LIMIT :pageSize OFFSET :offset
            """)
    Flux<Item> findAllWithParametersOrderByTitle(String search, int pageSize, long offset);

    @Query("""
                SELECT * FROM items 
                WHERE (title ILIKE '%' || :search || '%' 
                       OR description ILIKE '%' || :search || '%')
                ORDER BY price ASC
                LIMIT :pageSize OFFSET :offset
            """)
    Flux<Item> findAllWithParametersOrderByPrice(String search, int pageSize, long offset);

    @Query("""
                SELECT * FROM items 
                WHERE (title ILIKE '%' || :search || '%' 
                       OR description ILIKE '%' || :search || '%')
                LIMIT :pageSize OFFSET :offset
            """)
    Flux<Item> findAllWithParameters(String search, int pageSize, long offset);
}
