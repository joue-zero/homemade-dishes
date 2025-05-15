import axios from 'axios';
import { DEFAULT_USER_BALANCE } from '../constants/AppConstants';

// Update to use the user service API directly
const API_URL = 'http://localhost:8081/api/users';
const BALANCE_API_URL = 'http://localhost:8084/api/balance';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    if (!userId) {
        throw new Error('User ID not found. Please login again.');
    }
    return {
        headers: {
            'Authorization': `Bearer ${token}`,
            'X-User-Id': userId
        }
    };
};

/**
 * Balance Service
 * Handles user balance operations by communicating with APIs
 */
export const balanceService = {
    /**
     * Initialize user balance if not exists
     * This will now just call getBalance which will initialize it on the server
     */
    initializeBalance: async () => {
        try {
            const balance = await balanceService.getBalance();
            return balance;
        } catch (error) {
            console.error('Error initializing balance:', error);
            return DEFAULT_USER_BALANCE; // Fallback to default in case of error
        }
    },

    /**
     * Get current user balance from the server
     */
    getBalance: async () => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }
            
            // Use either the order service balance API or the user service directly
            const response = await axios.get(`${API_URL}/${userId}/balance`);
            return response.data;
        } catch (error) {
            console.error('Error getting balance:', error);
            throw error.response?.data || error.message;
        }
    },

    /**
     * Update user balance on the server (subtract payment amount)
     */
    updateBalance: async (amount) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }
            
            // Use the order service balance API which will forward to user service
            const response = await axios.post(
                `${BALANCE_API_URL}/update?amount=${amount}`, 
                null, 
                getAuthHeader()
            );
            return response.data.balance;
        } catch (error) {
            console.error('Error updating balance:', error);
            throw error.response?.data || error.message;
        }
    },

    /**
     * Check if user has sufficient balance for an amount
     */
    hasSufficientBalance: async (amount) => {
        try {
            // Use the order service's balance check API
            const response = await axios.get(
                `${BALANCE_API_URL}/check?amount=${amount}`, 
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            console.error('Error checking balance:', error);
            throw error.response?.data || error.message;
        }
    }
}; 