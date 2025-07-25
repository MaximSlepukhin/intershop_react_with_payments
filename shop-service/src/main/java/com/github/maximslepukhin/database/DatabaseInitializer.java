package com.github.maximslepukhin.database;


import com.github.maximslepukhin.model.Item;
import com.github.maximslepukhin.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    public DatabaseInitializer(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Item> items = List.of(
                new Item(null, "Ноутбук Lenovo", "15.6\", 16 ГБ RAM, SSD 512 ГБ", 80000, "images/lenovo.jpeg"),
                new Item(null, "Смартфон Samsung", "6.5\" AMOLED, 128 ГБ памяти'", 90000, "images/samsung.jpeg"),
                new Item(null, "Наушники Sony", "Беспроводные, шумоподавление", 20000, "images/sony.jpeg"),
                new Item(null, "Фотоаппарат Canon", "Зеркальный, 24.2 МП", 120000, "images/canon.jpeg"),
                new Item(null, "Умные часы Apple", "Watch Series 8, GPS'", 40000, "images/apple.jpeg"),
                new Item(null, "Планшет iPad Pro", "12.9\", 256 ГБ, M1", 95000, "images/ipad.jpeg"),
                new Item(null, "Монитор Dell", "27\", 4K, IPS", 65000, "images/dell.jpeg"),
                new Item(null, "Клавиатура Logitech", "Механическая, RGB", 12000, "images/logitech.jpeg"),
                new Item(null, "Мышь Razer", "Игровая, 16000 DPI", 8000, "images/razer.jpeg"),
                new Item(null, "Колонка JBL", "Портативная, waterproof", 15000, "images/jbl.jpeg"),
                new Item(null, "Электронная книга Kindle", "10\", 32 ГБ", 25000, "images/kindle.jpeg"),
                new Item(null, "Роутер TP-Link", "Wi-Fi 6, 3000 Мбит/с", 18000, "images/tplink.jpeg")
        );

        itemRepository.saveAll(items)
                .doOnNext(savedItem -> System.out.println("Saved item: " + savedItem.getTitle()))
                .blockLast();
    }
}
