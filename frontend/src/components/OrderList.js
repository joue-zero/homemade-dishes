import React, { useState, useEffect } from 'react';
import axios from 'axios';

function OrderList() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const user = JSON.parse(localStorage.getItem('user'));
      const token = localStorage.getItem('token');
      
      const response = await axios.get(`http://localhost:8084/api/orders/customer/${user.id}`);
      setOrders(response.data);
    } catch (error) {
      console.error('Error fetching orders:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>My Orders</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {orders.map(order => (
          <div key={order.id} style={{ border: '1px solid #ddd', padding: '1rem', borderRadius: '4px' }}>
            <h3>Order #{order.id}</h3>
            <p>Status: {order.status}</p>
            <p>Total Amount: ${order.totalAmount}</p>
            <p>Created At: {new Date(order.createdAt).toLocaleString()}</p>
            <div>
              <h4>Items:</h4>
              {order.orderItems.map(item => (
                <div key={item.id} style={{ marginLeft: '1rem' }}>
                  <p>Dish ID: {item.dishId}</p>
                  <p>Quantity: {item.quantity}</p>
                  <p>Price: ${item.price}</p>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default OrderList; 