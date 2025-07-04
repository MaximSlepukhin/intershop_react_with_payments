package com.github.maximslepukhin.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;


@Table("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private Long id;
    @Column("total_sum")
    private double totalSum;

    @Transient
    private List<OrderItem> orderItems;

    public Long id() {
        return getId();
    }

    public double totalSum() {
        return getTotalSum();
    }

    public List<OrderItem> getItems() {
        return orderItems;
    }
}