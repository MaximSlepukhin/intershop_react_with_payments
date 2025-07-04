package com.github.maximslepukhin.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "orders_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    private Integer count;
}