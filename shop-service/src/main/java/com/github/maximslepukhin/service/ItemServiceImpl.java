package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.ItemWithCount;
import com.github.maximslepukhin.enums.SortType;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.repository.ItemRepository;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;

    private static final String ITEM_REDIS_KEY_PREFIX = "items:";

    public ItemServiceImpl(ItemRepository itemRepository, ReactiveRedisTemplate<String, Item> redisTemplate) {
        this.itemRepository = itemRepository;
        this.itemRedisTemplate = redisTemplate;
    }

    @Override
    public Flux<Item> findItems(String search, SortType sortType, int page, int size) {
        String key = String.format("%ssearch:%s:sort:%s:page:%d:pageSize:%d:",
                ITEM_REDIS_KEY_PREFIX, search, sortType, page, size);

        ReactiveListOperations<String, Item> listOps = itemRedisTemplate.opsForList();
        Duration ttl = Duration.ofMinutes(10);

        Mono<List<Item>> listMono =
                listOps.range(key, 0, -1)
                        .collectList()
                        .filter(list -> !list.isEmpty())
                        .switchIfEmpty(
                                findItemsFromRepository(search, sortType, page, size)
                                        .collectList()
                                        .delayUntil(list ->
                                                listOps.rightPushAll(key, list)
                                                        .then(itemRedisTemplate.expire(key, ttl)))
                        )
                        .cache();

        return listMono.flatMapMany(Flux::fromIterable);
    }

    public Flux<Item> findItemsFromRepository(String search, SortType sortType, int page, int size) {
        int offset = (page - 1) * size;
        return switch (sortType) {
            case ALPHA -> itemRepository.findAllWithParametersOrderByTitle(search, size + 1, offset);
            case PRICE -> itemRepository.findAllWithParametersOrderByPrice(search, size + 1, offset);
            case NO -> itemRepository.findAllWithParameters(search, size + 1, offset);
        };
    }

    @Override
    public Flux<ItemWithCount> toItemsWithCount(Flux<Item> items, Map<Long, Integer> cart) {
        return items.map(item -> {
            int count = cart.getOrDefault(item.getId(), 0);
            return new ItemWithCount(item, count);
        });
    }

    @Override
    public ItemWithCount[][] splitToRows(List<ItemWithCount> itemsWithCount, int elementsInRows) {
        int rowsCount = (itemsWithCount.size() + elementsInRows - 1) / elementsInRows;
        ItemWithCount[][] result = new ItemWithCount[rowsCount][];

        for (int i = 0; i < rowsCount; i++) {
            int start = i * elementsInRows;
            int end = Math.min(start + elementsInRows, itemsWithCount.size());
            result[i] = itemsWithCount.subList(start, end).toArray(new ItemWithCount[0]);
        }
        return result;
    }

    @Override
    public Mono<Item> getItemById(Long id) {
        String key = ITEM_REDIS_KEY_PREFIX + id;
        return itemRedisTemplate.opsForValue().get(key)
                .doOnNext(item -> System.out.println("Cache HIT for key: " + key))
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .flatMap(item -> itemRedisTemplate
                                        .opsForValue()
                                        .set(key, item, Duration.ofMinutes(3))
                                        .thenReturn(item)
                                )
                );
    }

    @Override
    public Flux<ItemWithCount> getItemWithCount(Map<Long, Integer> cart) {
        return getItems(cart)
                .map(item -> {
                    int count = cart.getOrDefault(item.getId(), 0);
                    return new ItemWithCount(item, count);
                });
    }
    @Override
    public Flux<Item> getItems(Map<Long, Integer> cart) {
        ReactiveValueOperations<String, Item> valueOps = itemRedisTemplate.opsForValue();
        Duration ttl = Duration.ofMinutes(10);

        return Flux.fromIterable(cart.keySet())
                .flatMap(id -> {
                    String key = ITEM_REDIS_KEY_PREFIX + id;
                    return valueOps.get(key)
                            .switchIfEmpty(
                                    itemRepository.findById(id)
                                            .delayUntil(item ->
                                                    valueOps.set(key, item).then(itemRedisTemplate.expire(key, ttl))
                                            )
                            );
                });
    }
}
