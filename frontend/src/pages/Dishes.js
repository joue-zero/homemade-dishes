import React, { useState, useEffect } from 'react';
import { dishService } from '../services/dishService';
import Cart from '../components/Cart';
import './Dishes.css';

const Dishes = () => {
  const [dishes, setDishes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cartItems, setCartItems] = useState([]);

  useEffect(() => {
    fetchDishes();
  }, []);

  const fetchDishes = async () => {
    try {
      const data = await dishService.getAllDishes();
      setDishes(data);
      setError('');
    } catch (err) {
      setError('Failed to fetch dishes. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (dish) => {
    setCartItems(prevItems => {
      const existingItem = prevItems.find(item => item.dishId === dish.id);
      if (existingItem) {
        return prevItems.map(item =>
          item.dishId === dish.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prevItems, {
        dishId: dish.id,
        name: dish.name,
        price: dish.price,
        quantity: 1
      }];
    });
  };

  const handleOrderComplete = () => {
    setCartItems([]);
  };

  if (loading) return <div className="loading">Loading dishes...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="dishes-page">
      <div className="dishes-container">
        <h1>Available Dishes</h1>
        <div className="dishes-grid">
          {dishes.map(dish => (
            <div key={dish.id} className="dish-card">
              {dish.imageUrl && (
                <img src={dish.imageUrl} alt={dish.name} className="dish-image" />
              )}
              <div className="dish-info">
                <h3>{dish.name}</h3>
                <p className="dish-description">{dish.description}</p>
                <div className="dish-footer">
                  <span className="dish-price">${dish.price.toFixed(2)}</span>
                  <button
                    className="add-to-cart"
                    onClick={() => handleAddToCart(dish)}
                    disabled={!dish.available}
                  >
                    {dish.available ? 'Add to Cart' : 'Not Available'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="cart-container">
        <Cart items={cartItems} onOrderComplete={handleOrderComplete} />
      </div>
    </div>
  );
};

export default Dishes; 