import axios from 'axios';

const API_URL = 'http://localhost:8083/api/orders';

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

export const orderService = {
    createOrder: async (items) => {
        try {
            const response = await axios.post(`${API_URL}/user-order`, items, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getUserOrders: async () => {
        try {
            const response = await axios.get(API_URL, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getOrder: async (orderId) => {
        try {
            const response = await axios.get(`${API_URL}/${orderId}`, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    updateOrderStatus: async (orderId, status) => {
        try {
            const response = await axios.put(
                `${API_URL}/${orderId}/status?status=${status}`,
                {},
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    cancelOrder: async (orderId) => {
        try {
            await axios.post(`${API_URL}/${orderId}/cancel`, {}, getAuthHeader());
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getOrdersBySeller: async (sellerId) => {
        try {
            const response = await axios.get(`${API_URL}/seller/${sellerId}`, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    }
}; 