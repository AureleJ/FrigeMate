import { config } from '../config/config.js';

class AuthService {
    constructor() {
        this.storageKeys = config.STORAGE_KEYS;
    }

    isAuthenticated() {
        return !!(this.getUserId() && this.getUsername());
    }

    getCurrentUser() {
        const userId = this.getUserId();
        const username = this.getUsername();
        return userId && username ? { userId, username } : null;
    }

    setAuth(userId, username) {
        localStorage.setItem(this.storageKeys.USER_ID, userId);
        localStorage.setItem(this.storageKeys.USERNAME, username);
        window.dispatchEvent(new Event('auth-change'));
    }

    logout() {
        localStorage.removeItem(this.storageKeys.USER_ID);
        localStorage.removeItem(this.storageKeys.USERNAME);
        window.dispatchEvent(new Event('auth-change'));
    }

    getUserId() {
        return localStorage.getItem(this.storageKeys.USER_ID);
    }

    getUsername() {
        return localStorage.getItem(this.storageKeys.USERNAME);
    }
}

export const authService = new AuthService();
