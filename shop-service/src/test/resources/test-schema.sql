DROP TABLE IF EXISTS orders_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS items CASCADE;

CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      total_sum NUMERIC(10,2) NOT NULL
    );

CREATE TABLE IF NOT EXISTS items (
                                     id BIGSERIAL PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    img_path VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS orders_items (
                                            order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    count INTEGER NOT NULL CHECK(count > 0),
    PRIMARY KEY(order_id, item_id)
    );