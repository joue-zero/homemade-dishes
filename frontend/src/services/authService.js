import axios from 'axios';

const API_URL = 'http://localhost:8081/api/users';

export const authService = {
    async login(credentials) {
        try {
            const response = await axios.post(`${API_URL}/login`, credentials);
            return response.data;
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data.message || 'Login failed');
            }
            throw new Error('Network error occurred');
        }
    },

    async register(userData) {
        try {
            const response = await axios.post(`${API_URL}/register`, userData);
            return response.data;
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data.message || 'Registration failed');
            }
            throw new Error('Network error occurred');
        }
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
    },

    getAuthHeader() {
        const token = localStorage.getItem('token');
        return token ? { Authorization: `Bearer ${token}` } : {};
    },

    isAuthenticated() {
        return !!localStorage.getItem('token');
    }
}; 