package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.dto.ItemWithCount;
import com.github.maximslepukhin.dto.ItemsResult;

import com.github.maximslepukhin.enums.SortType;
import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.model.SecurityUser;
import com.github.maximslepukhin.service.CartService;
import com.github.maximslepukhin.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;
    private static final int ELEMENTS_IN_ROWS = 3;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> showItems(@AuthenticationPrincipal SecurityUser securityUser,
                                  @RequestParam(defaultValue = "") String search,
                                  @RequestParam(defaultValue = "NO") SortType sort,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(defaultValue = "1") int pageNumber,
                                  Model model) {

        Mono<Map<Long, Integer>> cartMono;
        boolean isAuthenticated = securityUser != null;

        if (securityUser != null) {
            Long userId = securityUser.getId();
            cartMono = cartService.getUserCartMap(userId).defaultIfEmpty(new HashMap<>());
        } else {
            cartMono = Mono.just(new HashMap<>());
        }

        return cartMono.flatMap(cart ->
                itemService.findItems(search, sort, pageNumber, pageSize)
                        .collectList()
                        .flatMap(items -> {
                            boolean hasNext = items.size() > pageSize;
                            List<Item> trimmedItems = hasNext ? items.subList(0, pageSize) : items;

                            return itemService.toItemsWithCount(Flux.fromIterable(trimmedItems), cart)
                                    .collectList()
                                    .map(itemsWithCount -> {
                                        ItemWithCount[][] splitItems = itemService.splitToRows(itemsWithCount, ELEMENTS_IN_ROWS);
                                        ItemsResult paging = new ItemsResult(hasNext, pageNumber, pageSize);
                                        model.addAttribute("isAuthenticated", isAuthenticated);
                                        model.addAttribute("items", splitItems);
                                        model.addAttribute("search", search);
                                        model.addAttribute("sort", sort.toString());
                                        model.addAttribute("paging", paging.getPageInfo());

                                        return "main";
                                    });
                        })
        );
    }

    @GetMapping("items/{id}")
    public Mono<String> showItem(@PathVariable Long id,
                                 @AuthenticationPrincipal SecurityUser securityUser,
                                 Model model) {

        Mono<Map<Long, Integer>> cartMono;
        boolean isAuthenticated = securityUser != null;

        if (securityUser != null) {
            Long userId = securityUser.getId();
            cartMono = cartService.getUserCartMap(userId).defaultIfEmpty(new HashMap<>());
        } else {
            cartMono = Mono.just(new HashMap<>());
        }
        return cartMono
                .flatMap(cart -> itemService.getItemById(id)
                        .flatMap(item -> {
                            int countFromCart = cart.getOrDefault(item.getId(), 0);
                            ItemWithCount itemWithCount = new ItemWithCount(item, countFromCart);
                            model.addAttribute("isAuthenticated", isAuthenticated);
                            model.addAttribute("item", itemWithCount);
                            return Mono.just("item");
                        })
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")));
    }
}
