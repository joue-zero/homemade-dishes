import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import './Cart.css';

const Cart = ({ items, onOrderComplete }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [cartItems, setCartItems] = useState([]);

    useEffect(() => {
        setCartItems(items);
    }, [items]);

    const handleQuantityChange = (dishId, change) => {
        setCartItems(prevItems => 
            prevItems.map(item => 
                item.dishId === dishId
                    ? { ...item, quantity: Math.max(1, item.quantity + change) }
                    : item
            )
        );
    };

    const removeItem = (dishId) => {
        setCartItems(prevItems => prevItems.filter(item => item.dishId !== dishId));
    };

    const calculateTotal = () => {
        return cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);
    };

    const handleOrder = async () => {
        if (cartItems.length === 0) {
            setError('Your cart is empty');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const orderItems = cartItems.map(item => ({
                dishId: item.dishId,
                quantity: item.quantity
            }));

            await orderService.createOrder(orderItems);
            setCartItems([]);
            onOrderComplete();
        } catch (err) {
            setError(err.message || 'Failed to create order');
        } finally {
            setLoading(false);
        }
    };

    if (cartItems.length === 0) {
        return (
            <div className="cart-empty">
                <p>Your cart is empty</p>
            </div>
        );
    }

    return (
        <div className="cart">
            <h2>Your Cart</h2>
            {error && <div className="error-message">{error}</div>}
            <div className="cart-items">
                {cartItems.map(item => (
                    <div key={item.dishId} className="cart-item">
                        <div className="item-info">
                            <h3>{item.name}</h3>
                            <p className="item-price">${item.price.toFixed(2)}</p>
                        </div>
                        <div className="item-quantity">
                            <button 
                                onClick={() => handleQuantityChange(item.dishId, -1)}
                                disabled={item.quantity <= 1}
                            >
                                -
                            </button>
                            <span>{item.quantity}</span>
                            <button 
                                onClick={() => handleQuantityChange(item.dishId, 1)}
                            >
                                +
                            </button>
                        </div>
                        <div className="item-subtotal">
                            ${(item.price * item.quantity).toFixed(2)}
                        </div>
                        <button 
                            className="remove-item"
                            onClick={() => removeItem(item.dishId)}
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
            <div className="cart-summary">
                <div className="total">
                    <span>Total:</span>
                    <span>${calculateTotal().toFixed(2)}</span>
                </div>
                <button 
                    className="order-button"
                    onClick={handleOrder}
                    disabled={loading}
                >
                    {loading ? 'Processing...' : 'Place Order'}
                </button>
            </div>
        </div>
    );
};

export default Cart; 