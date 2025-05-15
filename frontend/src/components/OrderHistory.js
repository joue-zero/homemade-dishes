import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import PaymentForm from './PaymentForm';
import './OrderHistory.css';

const OrderHistory = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [paymentOrder, setPaymentOrder] = useState(null);
    const [paymentSuccess, setPaymentSuccess] = useState(false);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await orderService.getUserOrders();
            setOrders(response);
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

    const handlePaymentClick = (order) => {
        setPaymentOrder(order);
        setPaymentSuccess(false);
    };

    const handlePaymentSuccess = (response) => {
        setPaymentSuccess(true);
        // Refresh orders to show updated status
        fetchOrders();
    };

    const handlePaymentError = (error) => {
        console.error('Payment error:', error);
        // Payment form will display the error
    };

    const handleClosePayment = () => {
        setPaymentOrder(null);
        setPaymentSuccess(false);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString();
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING':
                return '#f57c00';
            case 'ACCEPTED':
                return '#1976d2';
            case 'READY':
                return '#9c27b0';
            case 'COMPLETED':
                return '#2e7d32';
            case 'CANCELLED':
                return '#c62828';
            case 'REJECTED':
                return '#d32f2f';
            default:
                return '#666';
        }
    };

    const getPaymentStatusBadge = (order) => {
        if (!order.paymentStatus) return null;
        
        let color, text;
        switch (order.paymentStatus) {
            case 'PAID':
                color = '#4caf50';
                text = 'Paid';
                break;
            case 'FAILED':
                color = '#f44336';
                text = 'Payment Failed';
                break;
            case 'PENDING':
                color = '#ff9800';
                text = 'Payment Pending';
                break;
            default:
                return null;
        }
        
        return (
            <span className="payment-status-badge" style={{ backgroundColor: color }}>
                {text}
            </span>
        );
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
            
            {paymentOrder && (
                <div className="payment-modal-overlay">
                    <div className="payment-modal">
                        <button className="close-modal-btn" onClick={handleClosePayment}>×</button>
                        
                        {paymentSuccess ? (
                            <div className="payment-success">
                                <h3>Payment Successful!</h3>
                                <p>Your order is now being prepared.</p>
                                <button className="close-btn" onClick={handleClosePayment}>Close</button>
                            </div>
                        ) : (
                            <PaymentForm 
                                order={paymentOrder} 
                                onSuccess={handlePaymentSuccess}
                                onError={handlePaymentError}
                            />
                        )}
                    </div>
                </div>
            )}
            
            <div className="orders-list">
                {orders.map(order => (
                    <div key={order.id} className="order-card">
                        <div className="order-header">
                            <div className="order-info">
                                <span className="order-id">Order #{order.id}</span>
                                <span className="order-date">{formatDate(order.createdAt)}</span>
                            </div>
                            <div className="status-container">
                                {order.paymentStatus === 'PAID' && (
                                    <div className="payment-badge paid">PAID</div>
                                )}
                                <div 
                                    className="order-status"
                                    style={{ backgroundColor: getStatusColor(order.status) }}
                                >
                                    {order.status}
                                </div>
                            </div>
                        </div>
                        <div className="order-items">
                            {order.items.map(item => (
                                <div key={item.id} className="order-item">
                                    <span className="item-name">{item.dishName}</span>
                                    <span className="item-quantity">x{item.quantity}</span>
                                    <span className="item-price">${Math.round(item.price * item.quantity)}</span>
                                </div>
                            ))}
                        </div>
                        <div className="order-footer">
                            <div className="order-total">
                                Total: ${order.totalAmount.toFixed(2)}
                            </div>
                            <div className="order-actions">
                                {order.status === 'PENDING' && (
                                    <button
                                        className="cancel-button"
                                        onClick={() => handleCancelOrder(order.id)}
                                    >
                                        Cancel Order
                                    </button>
                                )}
                                
                                {order.status === 'ACCEPTED' && order.paymentStatus !== 'PAID' && (
                                    <button
                                        className="pay-button"
                                        onClick={() => handlePaymentClick(order)}
                                    >
                                        Pay Now
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default OrderHistory; 