-- Add seller_id column to order_items table
ALTER TABLE order_items ADD COLUMN seller_id BIGINT;

-- Temporarily update order_items with seller_id from orders table
UPDATE order_items oi 
SET seller_id = (SELECT seller_id FROM orders o WHERE o.id = oi.order_id)
WHERE EXISTS (SELECT 1 FROM orders o WHERE o.id = oi.order_id AND o.seller_id IS NOT NULL);

-- Make seller_id NOT NULL in order_items after data migration
-- Comment this out if you want to make it optional initially
ALTER TABLE order_items MODIFY COLUMN seller_id BIGINT NOT NULL;

-- Drop seller_id column from orders table
ALTER TABLE orders DROP COLUMN seller_id; 