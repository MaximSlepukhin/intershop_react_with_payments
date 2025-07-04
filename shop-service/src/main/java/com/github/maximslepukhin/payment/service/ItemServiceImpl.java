package com.github.maximslepukhin.payment.service;

import com.github.maximslepukhin.payment.dto.ItemWithCount;
import com.github.maximslepukhin.payment.enums.SortType;
import com.github.maximslepukhin.payment.model.Item;
import com.github.maximslepukhin.payment.repository.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Flux<Item> findItems(SortType sortType, String searchTitle, String searchDescription) {
        Flux<Item> items = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTitle, searchDescription);

        return switch (sortType) {
            case ALPHA -> items.sort(Comparator.comparing(Item::getTitle, String.CASE_INSENSITIVE_ORDER));
            case PRICE -> items.sort(Comparator.comparingDouble(Item::getPrice));
            case NO -> items;
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
        return itemRepository.findById(id);
    }

    @Override
    public Flux<Item> getItems(Map<Long, Integer> cart) {
        return Flux.fromIterable(cart.keySet())
                .flatMap(itemRepository::findById);
    }

    @Override
    public Flux<ItemWithCount> getItemWithCount(Map<Long, Integer> cart) {
        return getItems(cart)
                .map(item -> {
                    int count = cart.getOrDefault(item.getId(), 0);
                    return new ItemWithCount(item, count);
                });
    }
}
