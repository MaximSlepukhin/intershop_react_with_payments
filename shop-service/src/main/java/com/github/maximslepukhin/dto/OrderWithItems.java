package com.github.maximslepukhin.dto;

import com.github.maximslepukhin.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderWithItems {

    private Long orderId;
    private double totalSum;
    private List<ItemWithCount> items;

    public OrderWithItems(Order order, List<ItemWithCount> itemWithCount) {
        this.orderId = order.getId();
        this.totalSum = order.getTotalSum();
        this.items = itemWithCount;
    }

    public Long id() {
        return getOrderId();
    }

    public List<ItemWithCount> items() {
        return getItems();
    }

    public double totalSum() {
        return getTotalSum();
    }
}