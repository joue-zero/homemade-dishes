import React, { useState, useEffect } from 'react';
import { paymentService } from '../services/paymentService';
import { balanceService } from '../services/balanceService';
import './PaymentForm.css';

const PaymentForm = ({ order, onSuccess, onError }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [userBalance, setUserBalance] = useState(0);
    
    useEffect(() => {
        // Initialize user balance
        const fetchBalance = async () => {
            try {
                const balance = await balanceService.initializeBalance();
                console.log("Fetched balance:", balance, "Order total:", order.totalAmount);
                // Handle the balance value which could be a raw number from user service
                setUserBalance(Number(balance));
            } catch (error) {
                console.error("Error fetching balance:", error);
                setError('Error loading balance. Please try again.');
            }
        };
        
        fetchBalance();
    }, [order.totalAmount]);

    const handlePayment = async (e) => {
        e.preventDefault();
        
        // Check balance first
        try {
            const hasBalance = await balanceService.hasSufficientBalance(order.totalAmount);
            if (!hasBalance) {
                setError('Insufficient balance to complete this payment');
                return;
            }
        } catch (error) {
            console.error("Balance check error:", error);
            setError('Error checking balance: ' + error);
            return;
        }

        setLoading(true);
        setError('');

        try {
            // Process the payment through the API
            const response = await paymentService.processPayment({
                orderId: order.id
            });
            
            if (response.success) {
                // Refresh user's balance after successful payment
                const newBalance = await balanceService.getBalance();
                // Ensure balance is a number
                setUserBalance(Number(newBalance));
                onSuccess(response);
            } else {
                setError(response.message || 'Payment failed');
                onError(response);
            }
        } catch (err) {
            const errorMessage = typeof err === 'string' ? err : err.message || 'Payment processing failed';
            setError(errorMessage);
            onError({ success: false, message: errorMessage });
        } finally {
            setLoading(false);
        }
    };

    // Check if button should be disabled
    const isDisabled = loading || userBalance < order.totalAmount;

    return (
        <div className="payment-form-container">
            <h3>Order Summary</h3>
            
            {error && <div className="payment-error">{error}</div>}
            
            <div className="balance-info">
                <span>Available Balance:</span>
                <span className="balance-amount">${userBalance.toFixed(2)}</span>
            </div>
            
            <div className="payment-form">
                <div className="form-group">
                    <label>Order Total</label>
                    <div className="order-total-display">${order.totalAmount.toFixed(2)}</div>
                </div>

                {isDisabled && userBalance < order.totalAmount && (
                    <div className="payment-error">
                        Your balance (${userBalance.toFixed(2)}) is less than the order total (${order.totalAmount.toFixed(2)}).
                    </div>
                )}

                <button 
                    onClick={handlePayment}
                    className="payment-submit-btn"
                    disabled={isDisabled}
                >
                    {loading ? 'Processing...' : 'Pay Now'}
                </button>
            </div>
        </div>
    );
};

export default PaymentForm; 