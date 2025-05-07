-- Clear existing data
DELETE FROM dishes;

-- Insert sample dishes
INSERT INTO dishes (name, description, price, category, image_url, available) VALUES
('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'PIZZA', 'https://example.com/margherita.jpg', true),
('Pepperoni Pizza', 'Pizza topped with pepperoni and cheese', 14.99, 'PIZZA', 'https://example.com/pepperoni.jpg', true),
('Chicken Burger', 'Grilled chicken burger with lettuce and special sauce', 9.99, 'BURGER', 'https://example.com/chicken-burger.jpg', true),
('Beef Burger', 'Classic beef burger with cheese and vegetables', 11.99, 'BURGER', 'https://example.com/beef-burger.jpg', true),
('Caesar Salad', 'Fresh romaine lettuce with Caesar dressing and croutons', 8.99, 'SALAD', 'https://example.com/caesar-salad.jpg', true),
('Greek Salad', 'Mixed vegetables with feta cheese and olive oil', 9.99, 'SALAD', 'https://example.com/greek-salad.jpg', true),
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 6.99, 'DESSERT', 'https://example.com/chocolate-cake.jpg', true),
('Cheesecake', 'Classic New York style cheesecake', 7.99, 'DESSERT', 'https://example.com/cheesecake.jpg', true),
('Pasta Carbonara', 'Creamy pasta with bacon and parmesan', 13.99, 'PASTA', 'https://example.com/carbonara.jpg', true),
('Spaghetti Bolognese', 'Pasta with meat sauce and parmesan', 12.99, 'PASTA', 'https://example.com/bolognese.jpg', true); 