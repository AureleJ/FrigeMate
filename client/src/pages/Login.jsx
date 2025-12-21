import React, { useState } from 'react';
import { apiService } from '../services/api';
import { authService } from '../services/auth';

const Login = () => {
    const [username, setUsername] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isRegisterMode, setIsRegisterMode] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            if (isRegisterMode) {
                const newUser = await apiService.register(username);
                if (newUser && newUser.id) {
                    authService.setAuth(newUser.id, newUser.username);
                } else {
                    setError('Registration failed');
                }
            } else {
                const response = await apiService.login(username);
                if (response.success && response.user) {
                    authService.setAuth(response.user.id, response.user.username);
                } else {
                    setError('Login failed');
                }
            }
        } catch (err) {
            setError(err.message || 'An error occurred');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <h1 className="app-title">FridgeMate</h1>
                    <p className="app-subtitle">
                        {isRegisterMode ? 'Create an account' : 'Smart Fridge Management'}
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <input 
                            type="text" 
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Username"
                            maxLength="50"
                            required 
                            disabled={isLoading}
                        />
                    </div>

                    {error && <div className="login-error">{error}</div>}

                    <button type="submit" className="btn btn-primary btn-login" disabled={isLoading}>
                        {isLoading ? 'Please wait...' : (isRegisterMode ? 'Sign Up' : 'Sign In')}
                    </button>

                    <div className="login-toggle-container">
                        {isRegisterMode ? 'Already have an account? ' : "Don't have an account? "}
                        <button 
                            type="button" 
                            className="btn-text"
                            onClick={() => {
                                setIsRegisterMode(!isRegisterMode);
                                setError('');
                            }}
                        >
                            {isRegisterMode ? 'Sign In' : 'Sign Up'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;
