import { apiService } from '../services/api.js';
import { authService } from '../services/auth.js';

class LoginController {
    constructor() {
        this.form = null;
        this.submitButton = null;
        this.isLoading = false;
    }

    init() {
        this.form = document.getElementById('loginForm');
        
        if (!this.form) {
            console.error('Login form not found');
            return;
        }

        this.submitButton = this.form.querySelector('button[type="submit"]');
        this.attachEvents();
        
        // Focus username field on load
        const usernameField = this.form.querySelector('input[name="username"]');
        if (usernameField) {
            usernameField.focus();
        }
    }

    attachEvents() {
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));
        
        // Enable submit on Enter key
        this.form.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !this.isLoading) {
                e.preventDefault();
                this.handleSubmit(e);
            }
        });
    }

    async handleSubmit(event) {
        event.preventDefault();
        
        if (this.isLoading) return;

        const formData = new FormData(this.form);
        const username = formData.get('username')?.trim();

        // Validate input
        if (!this.validateInput(username)) return;

        await this.attemptLogin(username);
    }

    validateInput(username) {
        if (!username) {
            this.showError('Please enter a username');
            return false;
        }

        if (username.length < 2) {
            this.showError('Username must be at least 2 characters');
            return false;
        }

        if (username.length > 50) {
            this.showError('Username too long (max 50 characters)');
            return false;
        }

        // Basic username validation (alphanumeric and some special chars)
        if (!/^[a-zA-Z0-9_.-]+$/.test(username)) {
            this.showError('Username can only contain letters, numbers, dots, hyphens and underscores');
            return false;
        }

        return true;
    }

    async attemptLogin(username) {
        this.setLoadingState(true);
        this.clearMessages();

        try {
            const response = await apiService.login(username);

            if (response.success && response.user) {
                authService.setAuth(response.user.id, response.user.username);
                this.showSuccess('Login successful! Redirecting...');
                
                // Small delay for UX, then redirect
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 500);
            } else {
                this.showError(response.message || 'Login failed. Please try again.');
            }
        } catch (error) {
            console.error('Login error:', error);
            
            let errorMessage = 'An error occurred during login. Please try again.';
            
            // Handle specific error cases
            if (error.message.includes('404')) {
                errorMessage = 'User not found. Please check your username.';
            } else if (error.message.includes('500')) {
                errorMessage = 'Server error. Please try again later.';
            } else if (error.message.includes('network')) {
                errorMessage = 'Network error. Please check your connection.';
            }
            
            this.showError(errorMessage);
        } finally {
            this.setLoadingState(false);
        }
    }

    setLoadingState(loading) {
        this.isLoading = loading;
        
        if (this.submitButton) {
            this.submitButton.disabled = loading;
            this.submitButton.textContent = loading ? 'Signing in...' : 'Sign In';
        }

        const usernameField = this.form.querySelector('input[name="username"]');
        if (usernameField) {
            usernameField.disabled = loading;
        }
    }

    showError(message) {
        this.showMessage(message, 'error');
    }

    showSuccess(message) {
        this.showMessage(message, 'success');
    }

    showMessage(message, type) {
        this.clearMessages();
        
        const messageEl = document.createElement('div');
        messageEl.className = `message message-${type}`;
        messageEl.textContent = message;
        
        this.form.insertBefore(messageEl, this.form.firstChild);
    }

    clearMessages() {
        const existingMessages = this.form.querySelectorAll('.message');
        existingMessages.forEach(msg => msg.remove());
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const loginController = new LoginController();
    loginController.init();
});