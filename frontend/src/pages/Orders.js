import React from 'react';
import OrderHistory from '../components/OrderHistory';

const Orders = () => {
    return (
        <div className="orders-page">
            <h1>My Orders</h1>
            <OrderHistory />
        </div>
    );
};

export default Orders; 