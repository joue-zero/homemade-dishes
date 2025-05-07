import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import './OrderHistory.css';

const OrderHistory = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await orderService.getUserOrders();
            setOrders(response);
            console.log(response);
        } catch (err) {
            setError(err.message || 'Failed to fetch orders');
        } finally {
            setLoading(false);
        }
    };

    const handleCancelOrder = async (orderId) => {
        try {
            await orderService.cancelOrder(orderId);
            fetchOrders(); // Refresh the orders list
        } catch (err) {
            setError(err.message || 'Failed to cancel order');
        }
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString();
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING':
                return '#f57c00';
            case 'CONFIRMED':
                return '#1976d2';
            case 'COMPLETED':
                return '#2e7d32';
            case 'CANCELLED':
                return '#c62828';
            default:
                return '#666';
        }
    };

    if (loading) {
        return <div className="loading">Loading orders...</div>;
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    if (orders.length === 0) {
        return <div className="no-orders">No orders found</div>;
    }

    return (
        <div className="order-history">
            <h2>Order History</h2>
            <div className="orders-list">
                {orders.map(order => (
                    <div key={order.id} className="order-card">
                        <div className="order-header">
                            <div className="order-info">
                                <span className="order-id">Order #{order.id}</span>
                                <span className="order-date">{formatDate(order.createdAt)}</span>
                            </div>
                            <div 
                                className="order-status"
                                style={{ backgroundColor: getStatusColor(order.status) }}
                            >
                                {order.status}
                            </div>
                        </div>
                        <div className="order-items">
                            {order.orderItems.map(item => (
                                <div key={item.id} className="order-item">
                                    <span className="item-name">{item.dishName}</span>
                                    <span className="item-quantity">x{item.quantity}</span>
                                    {/* <span className="item-price">${item.totalAmount.toFixed(2)}</span> */}
                                </div>
                            ))}
                        </div>
                        <div className="order-footer">
                            <div className="order-total">
                                Total: ${order.totalAmount.toFixed(2)}
                            </div>
                            {order.status === 'PENDING' && (
                                <button
                                    className="cancel-button"
                                    onClick={() => handleCancelOrder(order.id)}
                                >
                                    Cancel Order
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default OrderHistory; 