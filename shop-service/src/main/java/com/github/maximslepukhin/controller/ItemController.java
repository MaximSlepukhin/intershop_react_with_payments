package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.dto.ItemWithCount;
import com.github.maximslepukhin.dto.ItemsResult;

import com.github.maximslepukhin.enums.SortType;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
public class ItemController {
    private final ItemService itemService;
    private static final String CART_SESSION_KEY = "cart";
    private static final int ELEMENTS_IN_ROWS = 3;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> showItems(@RequestParam(defaultValue = "") String search,
                                  @RequestParam(defaultValue = "NO") SortType sort,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(defaultValue = "1") int pageNumber,
                                  WebSession session,
                                  Model model) {
        Map<Long, Integer> cart = Optional
                .ofNullable((Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(HashMap::new);

        return itemService.findItems(search, sort, pageNumber, pageSize)
                .collectList()
                .flatMap(items -> {
                    boolean hasNext = items.size() > pageSize;
                    List<Item> trimmedItems = hasNext ? items.subList(0, pageSize) : items;
                    return itemService.toItemsWithCount(Flux.fromIterable(trimmedItems), cart)
                            .collectList()
                            .map(itemsWithCount -> {
                                ItemWithCount[][] splitItems = itemService.splitToRows(itemsWithCount, ELEMENTS_IN_ROWS);
                                ItemsResult paging = new ItemsResult(hasNext, pageNumber, pageSize);
                                model.addAttribute("items", splitItems);
                                model.addAttribute("search", search);
                                model.addAttribute("sort", sort.toString());
                                model.addAttribute("paging", paging.getPageInfo());
                                return "main";
                            });
                });
    }

    @GetMapping("items/{id}")
    public Mono<String> showItem(@PathVariable Long id,
                                 Model model, WebSession session) {
        Map<Long, Integer> cart = Optional.ofNullable((Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(HashMap::new);

        return itemService.getItemById(id)
                .map(item -> {
                    int countFromCart = cart.getOrDefault(item.getId(), 0);
                    return new ItemWithCount(item, countFromCart);
                })
                .map(itemWithCount -> {
                    model.addAttribute("item", itemWithCount);
                    return "item";
                });
    }
}
