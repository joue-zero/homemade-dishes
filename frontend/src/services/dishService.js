import axios from 'axios';

const API_URL = 'http://localhost:8082/api/dishes';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    };
};

export const dishService = {
    getAllDishes: async () => {
        try {
            const response = await axios.get(API_URL);
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getDish: async (dishId) => {
        try {
            const response = await axios.get(`${API_URL}/${dishId}`);
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    createDish: async (dishData) => {
        try {
            const response = await axios.post(API_URL, dishData, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    updateDish: async (dishId, dishData) => {
        try {
            const response = await axios.put(`${API_URL}/${dishId}`, dishData, getAuthHeader());
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    deleteDish: async (dishId) => {
        try {
            await axios.delete(`${API_URL}/${dishId}`, getAuthHeader());
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    updateAvailability: async (dishId, available) => {
        try {
            const response = await axios.patch(
                `${API_URL}/${dishId}/availability`,
                { available },
                getAuthHeader()
            );
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    }
}; 