import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import './SalesHistory.css';

const SalesHistory = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchSellerOrders();
    }, []);

    const fetchSellerOrders = async () => {
        try {
            // Get seller ID from localStorage
            const sellerId = localStorage.getItem('userId');
            if (!sellerId) {
                setError('Seller ID not found. Please login again.');
                setLoading(false);
                return;
            }

            // Fetch orders for this seller
            const response = await orderService.getOrdersBySeller(sellerId);
            
            // Filter to only show orders with PAID payment status
            const paidOrders = response.filter(order => order.paymentStatus === 'PAID');
            
            // Transform orders to ensure they have all required properties
            const transformedOrders = paidOrders.map(order => ({
                ...order,
                // Use sellerItems (new API) and fall back to items (old API) if needed
                items: order.sellerItems || order.items || []
            }));
            
            setOrders(transformedOrders);
        } catch (err) {
            setError(err.message || 'Failed to fetch sales data');
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString();
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'ACCEPTED':
                return '#1976d2';
            case 'COMPLETED':
                return '#2e7d32';
            default:
                return '#666';
        }
    };

    if (loading) {
        return <div className="loading">Loading sales history...</div>;
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    if (orders.length === 0) {
        return <div className="no-orders">No paid orders found</div>;
    }

    return (
        <div className="sales-history">
            <h2>Sales History</h2>
            <p className="sales-subtitle">Orders with payment status: PAID</p>
            
            <div className="sales-list">
                {orders.map(order => (
                    <div key={order.id} className="sales-card">
                        <div className="sales-header">
                            <div className="sales-info">
                                <span className="order-id">Order #{order.id}</span>
                                <span className="order-date">{formatDate(order.createdAt)}</span>
                                <span className="customer-name">Customer: {order.customerName}</span>
                            </div>
                            <div 
                                className="order-status"
                                style={{ backgroundColor: getStatusColor(order.status) }}
                            >
                                {order.status}
                            </div>
                        </div>
                        <div className="order-items">
                            {order.items.map(item => (
                                <div key={item.id || `${item.dishId}-${item.quantity}`} className="order-item">
                                    <span className="item-name">{item.dishName}</span>
                                    <span className="item-quantity">x{item.quantity}</span>
                                    <span className="item-price">${(item.price * item.quantity).toFixed(2)}</span>
                                </div>
                            ))}
                        </div>
                        <div className="sales-footer">
                            <div className="order-total">
                                {order.sellerSubtotal ? (
                                    <span>Your Total: ${order.sellerSubtotal.toFixed(2)}</span>
                                ) : (
                                    <span>Total: ${order.totalAmount ? order.totalAmount.toFixed(2) : '0.00'}</span>
                                )}
                            </div>
                            <div className="payment-badge paid">Payment: PAID</div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SalesHistory; 