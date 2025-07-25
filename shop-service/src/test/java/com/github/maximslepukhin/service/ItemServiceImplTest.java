package com.github.maximslepukhin.service;


import com.github.maximslepukhin.dto.ItemWithCount;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private ReactiveRedisTemplate redisTemplate;
    private ReactiveListOperations listOperations;
    private ReactiveValueOperations valueOperations;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        redisTemplate = mock(ReactiveRedisTemplate.class);
        listOperations = mock(ReactiveListOperations.class);
        valueOperations = mock(ReactiveValueOperations.class);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        itemService = new ItemServiceImpl(itemRepository, redisTemplate);
    }

    @Test
    void testSplitToRowsEven() {
        List<ItemWithCount> list = Arrays.asList(
                new ItemWithCount(new Item(1L, "Item1", "", 10.0, ""), 1),
                new ItemWithCount(new Item(2L, "Item2", "", 20.0, ""), 1),
                new ItemWithCount(new Item(3L, "Item3", "", 30.0, ""), 1),
                new ItemWithCount(new Item(4L, "Item4", "", 40.0, ""), 1)
        );

        ItemWithCount[][] result = itemService.splitToRows(list, 2);
        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
        assertEquals("Item1", result[0][0].getTitle());
    }

    @Test
    void testSplitToRowsUneven() {
        List<ItemWithCount> list = Arrays.asList(
                new ItemWithCount(new Item(1L, "Item1", "", 10.0, ""), 1),
                new ItemWithCount(new Item(2L, "Item2", "", 20.0, ""), 1),
                new ItemWithCount(new Item(3L, "Item3", "", 30.0, ""), 1)
        );

        ItemWithCount[][] result = itemService.splitToRows(list, 2);
        assertEquals(2, result.length);
        assertEquals(1, result[1].length);
        assertEquals("Item3", result[1][0].getTitle());
    }

    @Test
    void testToItemsWithCount() {
        Map<Long, Integer> cart = Map.of(1L, 2, 2L, 3);
        Item item1 = new Item(1L, "Item1", "", 10.0, "");
        Item item2 = new Item(2L, "Item2", "", 20.0, "");

        Flux<Item> itemFlux = Flux.just(item1, item2);

        List<ItemWithCount> result = itemService.toItemsWithCount(itemFlux, cart).collectList().block();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getCount());
        assertEquals(3, result.get(1).getCount());
    }

    @Test
    void testGetItemById_CacheMissThenSave() {
        Item item = new Item(1L, "DbItem", "", 150.0, "");
        when(valueOperations.get(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(valueOperations.set(anyString(), eq(item), any())).thenReturn(Mono.just(true));

        Item result = itemService.getItemById(1L).block();

        assertNotNull(result);
        assertEquals("DbItem", result.getTitle());
    }
}
