import axios from 'axios';
import { DEFAULT_USER_BALANCE } from '../constants/AppConstants';

const API_URL = 'http://localhost:8083/api/balance';

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
 * Simple simulation using localStorage to manage user balance
 */
const BALANCE_KEY = 'user_balance';

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
            const response = await axios.get(API_URL, getAuthHeader());
            return response.data.balance;
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
            const response = await axios.post(
                `${API_URL}/update?amount=${amount}`, 
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
            const response = await axios.get(
                `${API_URL}/check?amount=${amount}`, 
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            console.error('Error checking balance:', error);
            throw error.response?.data || error.message;
        }
    }
}; 