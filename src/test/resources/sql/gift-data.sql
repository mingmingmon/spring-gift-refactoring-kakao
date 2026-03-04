INSERT INTO member (id, email) VALUES (1, 'sender@test.com');
INSERT INTO member (id, email) VALUES (2, 'receiver@test.com');
INSERT INTO category (id, name, color, image_url) VALUES (1, '테스트카테고리', '#000000', 'http://image.png');
INSERT INTO product (id, name, price, image_url, category_id) VALUES (1, '테스트상품', 10000, 'http://image.png', 1);
INSERT INTO options (id, name, quantity, product_id) VALUES (1, '기본옵션', 10, 1);
