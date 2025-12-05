import { config } from '../config/config.js';
import { authService } from './auth.js';

class ApiService {
    constructor() {
        this.baseURL = config.API_URL;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const requestOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, requestOptions);
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || errorData.error || `HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`API Error for ${endpoint}:`, error.message);
            throw error;
        }
    }

    async login(username) {
        return this.request('/users/login', {
            method: 'POST',
            body: JSON.stringify({ username })
        });
    }

    async register(username) {
        return this.request('/users', {
            method: 'POST',
            body: JSON.stringify({ username })
        });
    }

    async getFridge() {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge`);
    }

    async updateFridge(fridgeData) {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge`, {
            method: 'PUT',
            body: JSON.stringify(fridgeData)
        });
    }

    async getIngredients() {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge/ingredients`);
    }

    async addIngredient(ingredientData) {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge/ingredients`, {
            method: 'POST',
            body: JSON.stringify(ingredientData)
        });
    }

    async removeIngredient(ingredientId) {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge/ingredients/${ingredientId}`, {
            method: 'DELETE'
        });
    }

    async updateIngredient(ingredientId, ingredientData) {
        const userId = authService.getUserId();
        return this.request(`/users/${userId}/fridge/ingredients/${ingredientId}`, {
            method: 'PUT',
            body: JSON.stringify(ingredientData)
        });
    }
}

export const apiService = new ApiService();