import { config } from '../config/config.js';

class AuthService {
    constructor() {
        this.storageKeys = config.STORAGE_KEYS;
    }

    isAuthenticated() {
        return this.getUserId() && this.getUsername();
    }

    getCurrentUser() {
        const userId = this.getUserId();
        const username = this.getUsername();
        return userId && username ? { userId, username } : null;
    }

    setAuth(userId, username) {
        localStorage.setItem(this.storageKeys.USER_ID, userId);
        localStorage.setItem(this.storageKeys.USERNAME, username);
    }

    logout() {
        localStorage.removeItem(this.storageKeys.USER_ID);
        localStorage.removeItem(this.storageKeys.USERNAME);
    }

    getUserId() {
        return localStorage.getItem(this.storageKeys.USER_ID);
    }

    getUsername() {
        return localStorage.getItem(this.storageKeys.USERNAME);
    }

    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'login.html';
            return false;
        }
        return true;
    }
}

export const authService = new AuthService();