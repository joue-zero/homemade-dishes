import React, { useState, useEffect } from 'react';
import axios from 'axios';

function DishList() {
  const [dishes, setDishes] = useState([]);
  const [newDish, setNewDish] = useState({
    name: '',
    description: '',
    price: '',
    quantity: ''
  });

  useEffect(() => {
    fetchDishes();
  }, []);

  const fetchDishes = async () => {
    try {
      const response = await axios.get('http://localhost:8082/api/dishes');
      setDishes(response.data);
    } catch (error) {
      console.error('Error fetching dishes:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      await axios.post('http://localhost:8082/api/dishes', {
        ...newDish,
        sellerId: user.id,
        price: parseFloat(newDish.price),
        quantity: parseInt(newDish.quantity)
      });
      setNewDish({ name: '', description: '', price: '', quantity: '' });
      fetchDishes();
    } catch (error) {
      alert('Error adding dish: ' + error.message);
    }
  };

  const handleChange = (e) => {
    setNewDish({
      ...newDish,
      [e.target.name]: e.target.value
    });
  };

  return (
    <div>
      <h2>Dishes</h2>
      
      {/* Add New Dish Form */}
      <div style={{ marginBottom: '2rem' }}>
        <h3>Add New Dish</h3>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxWidth: '400px' }}>
          <input
            type="text"
            name="name"
            placeholder="Dish Name"
            value={newDish.name}
            onChange={handleChange}
            required
            style={{ padding: '0.5rem' }}
          />
          <textarea
            name="description"
            placeholder="Description"
            value={newDish.description}
            onChange={handleChange}
            required
            style={{ padding: '0.5rem' }}
          />
          <input
            type="number"
            name="price"
            placeholder="Price"
            value={newDish.price}
            onChange={handleChange}
            required
            style={{ padding: '0.5rem' }}
          />
          <input
            type="number"
            name="quantity"
            placeholder="Quantity"
            value={newDish.quantity}
            onChange={handleChange}
            required
            style={{ padding: '0.5rem' }}
          />
          <button type="submit" style={{ padding: '0.5rem', backgroundColor: '#28a745', color: 'white', border: 'none' }}>
            Add Dish
          </button>
        </form>
      </div>

      {/* Dish List */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
        {dishes.map(dish => (
          <div key={dish.id} style={{ border: '1px solid #ddd', padding: '1rem', borderRadius: '4px' }}>
            <h3>{dish.name}</h3>
            <p>{dish.description}</p>
            <p>Price: ${dish.price}</p>
            <p>Available: {dish.quantity}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default DishList; 