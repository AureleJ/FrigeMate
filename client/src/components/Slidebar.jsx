import React from 'react';
import { authService } from '../services/auth';

const Sidebar = ({ user, currentView, navigateTo }) => {
    const navItems = [
        { id: 'fridge', name: "My Fridge", icon: "📦" },
        { id: 'ingredients', name: "Ingredients", icon: "🥕" },
        { id: 'recipes', name: "Recipes", icon: "🍳" },
        { id: 'settings', name: "Settings", icon: "⚙️" },
    ];

    const handleLogout = () => {
        authService.logout();
    };

    return (
        <div className="sidebar">
            <div>
                <h1 className="app-title">FridgeMate</h1>
                
                <nav className="nav-links">
                    {navItems.map(item => (
                        <li key={item.id} className="nav-item">
                            <a 
                                href="#" 
                                className={currentView === item.id ? 'active' : ''} 
                                onClick={(e) => {
                                    e.preventDefault();
                                    navigateTo(item.id);
                                }}
                            >
                                {item.icon}
                                {item.name}
                            </a>
                        </li>
                    ))}
                </nav>
            </div>

            <div className="user-profile-sidebar">
                <div className="avatar">{user?.username?.[0].toUpperCase()}</div>
                <div>
                    <div>{user?.username}</div>
                    <button onClick={handleLogout} className="btn-logout" style={{padding: '0', fontSize: '0.75rem'}}>
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Sidebar;