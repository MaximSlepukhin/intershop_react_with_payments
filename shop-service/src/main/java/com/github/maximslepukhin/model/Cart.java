package com.github.maximslepukhin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    private Long id;
    @Column("item_id")
    private Long itemId;
    @Column("user_id")
    private Long userId;
    private Integer count;
}
