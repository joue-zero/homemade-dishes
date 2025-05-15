import React, { useState, useEffect } from 'react';
import { paymentService } from '../services/paymentService';
import { balanceService } from '../services/balanceService';
import './PaymentForm.css';

const PaymentForm = ({ order, onSuccess, onError }) => {
    const [paymentData, setPaymentData] = useState({
        orderId: order.id,
        paymentMethod: 'Credit Card',
        cardNumber: '',
        expiryDate: '',
        cvv: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [userBalance, setUserBalance] = useState(0);
    
    useEffect(() => {
        // Initialize user balance
        const fetchBalance = async () => {
            try {
                const balance = await balanceService.initializeBalance();
                setUserBalance(balance);
            } catch (error) {
                setError('Error loading balance. Please try again.');
            }
        };
        
        fetchBalance();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setPaymentData(prevData => ({
            ...prevData,
            [name]: value
        }));
    };

    const validateForm = async () => {
        // Check user balance
        try {
            const hasBalance = await balanceService.hasSufficientBalance(order.totalAmount);
            if (!hasBalance) {
                setError('Insufficient balance to complete this payment');
                return false;
            }
        } catch (error) {
            setError('Error checking balance: ' + error);
            return false;
        }
        
        // Basic validation
        if (!paymentData.cardNumber.trim()) {
            setError('Card number is required');
            return false;
        }
        if (!paymentData.expiryDate.trim()) {
            setError('Expiry date is required');
            return false;
        }
        if (!paymentData.cvv.trim()) {
            setError('CVV is required');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const isValid = await validateForm();
        if (!isValid) return;

        setLoading(true);
        setError('');

        try {
            // Process the payment through the API
            const response = await paymentService.processPayment(paymentData);
            
            if (response.success) {
                // Refresh user's balance after successful payment
                const newBalance = await balanceService.getBalance();
                setUserBalance(newBalance);
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

    return (
        <div className="payment-form-container">
            <h3>Payment Details</h3>
            
            {error && <div className="payment-error">{error}</div>}
            
            <div className="balance-info">
                <span>Your Balance:</span>
                <span className="balance-amount">${userBalance.toFixed(2)}</span>
            </div>
            
            <form onSubmit={handleSubmit} className="payment-form">
                <div className="form-group">
                    <label>Order Total</label>
                    <div className="order-total-display">${order.totalAmount.toFixed(2)}</div>
                </div>

                <div className="form-group">
                    <label htmlFor="cardNumber">Card Number</label>
                    <input
                        type="text"
                        id="cardNumber"
                        name="cardNumber"
                        placeholder="1234 5678 9012 3456"
                        value={paymentData.cardNumber}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="expiryDate">Expiry Date</label>
                        <input
                            type="text"
                            id="expiryDate"
                            name="expiryDate"
                            placeholder="MM/YY"
                            value={paymentData.expiryDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="cvv">CVV</label>
                        <input
                            type="text"
                            id="cvv"
                            name="cvv"
                            placeholder="123"
                            value={paymentData.cvv}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <button 
                    type="submit" 
                    className="payment-submit-btn"
                    disabled={loading || userBalance < order.totalAmount}
                >
                    {loading ? 'Processing...' : 'Pay Now'}
                </button>
            </form>
        </div>
    );
};

export default PaymentForm; 