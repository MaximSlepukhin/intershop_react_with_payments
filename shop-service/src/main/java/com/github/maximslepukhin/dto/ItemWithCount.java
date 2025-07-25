package com.github.maximslepukhin.dto;


import com.github.maximslepukhin.model.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemWithCount extends Item {

    private int count;

    public ItemWithCount(Item item, int count) {
        super(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getImgPath()
        );
        this.count = count;
    }
}
