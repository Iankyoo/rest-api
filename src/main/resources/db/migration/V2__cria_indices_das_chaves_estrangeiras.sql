-- O Postgres não cria índice para chave estrangeira automaticamente.
-- Sem eles, buscar os itens de uma comanda (ou as comandas de uma mesa) faz full scan.

CREATE INDEX idx_menu_item_categories_category ON menu_item_categories (category_id);
CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_restaurant_table ON orders (restaurant_table_id);
CREATE INDEX idx_order_item_order ON order_item (order_id);
CREATE INDEX idx_order_item_menu_item ON order_item (menu_item_id);
