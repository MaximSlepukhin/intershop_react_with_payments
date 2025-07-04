package com.github.maximslepukhin.payment.service;


import com.github.maximslepukhin.payment.dto.ItemWithCount;
import com.github.maximslepukhin.payment.enums.SortType;
import com.github.maximslepukhin.payment.model.Item;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ItemService {

    Mono<Item> getItemById(Long id);

    Flux<Item> getItems(Map<Long, Integer> cart);

    Flux<ItemWithCount> getItemWithCount(Map<Long, Integer> cart);

    Flux<Item> findItems(SortType sortType, String searchTitle, String searchDescription);

    Flux<ItemWithCount> toItemsWithCount(Flux<Item> items, Map<Long, Integer> cart);

    ItemWithCount[][] splitToRows(List<ItemWithCount> itemsWithCount, int elementsInRows);
}