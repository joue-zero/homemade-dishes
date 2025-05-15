import axios from 'axios';

const API_URL = 'http://localhost:8083/api/payments';

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

export const paymentService = {
    processPayment: async (paymentData) => {
        try {
            const response = await axios.post(
                `${API_URL}/process`, 
                paymentData, 
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getPaymentStatus: async (orderId) => {
        try {
            const response = await axios.get(
                `${API_URL}/status/${orderId}`, 
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    }
}; 