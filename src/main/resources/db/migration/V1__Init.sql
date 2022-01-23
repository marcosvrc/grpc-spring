CREATE TABLE PRODUCT(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price FLOAT NOT NULL,
    quantity_in_stock INTEGER NOT NULL,
    CONSTRAINT id UNIQUE (id)
);

INSERT INTO PRODUCT(id, name, price, quantity_in_stock) VALUES (1, 'Computador', 1234.00, 100);
INSERT INTO PRODUCT(id, name, price, quantity_in_stock) VALUES (2, 'Celular', 543.98, 600);