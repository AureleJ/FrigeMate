import React, { useState, useEffect } from 'react';
import { authService } from '../services/auth';
import Sidebar from '../components/Slidebar';
import MyFridge from './MyFridge';
import Ingredients from './Ingredients';
import { apiService } from '../services/api';

const Dashboard = () => {
    const user = authService.getCurrentUser();
    const [currentView, setCurrentView] = useState('fridge');
    const [ingredients, setIngredients] = useState([]);
    const [fridge, setFridge] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadIngredients = async () => {
        try {
            const data = await apiService.getIngredients();
            setIngredients(data.ingredients || []);
        } catch (err) {
            setError('Failed to load ingredients');
            console.error(err);
        }
    };

    const loadFridge = async () => {
        try {
            const data = await apiService.getFridge();
            setFridge(data.fridge || null);
            console.log('Loaded fridge data:', data);
        } catch (err) {
            console.error('Failed to load fridge data', err);
        }
    }

    const handleAddIngredient = async (newIngredient) => {
        try {
            await apiService.addIngredient(newIngredient);
            loadIngredients();
        } catch (err) {
            console.error('Failed to add ingredient', err);
            alert('Failed to add ingredient');
        }
    };

    const handleRemoveIngredient = async (id) => {
        try {
            await apiService.removeIngredient(id);
            setIngredients(prev => prev.filter(item => item.id !== id));
        } catch (err) {
            console.error('Failed to remove ingredient', err);
            alert('Failed to remove ingredient');
        }
    };

    useEffect(() => {
        setLoading(true);
        loadFridge();
        loadIngredients();
        setLoading(false);
    }, []);

    const renderContent = () => {
        switch (currentView) {
            case 'fridge':
                return <MyFridge fridge={fridge} ingredients={ingredients} onRemove={handleRemoveIngredient} onAdd={handleAddIngredient} error={error} />;
            case 'ingredients':
                return <Ingredients ingredients={ingredients} onRemove={handleRemoveIngredient} onAdd={handleAddIngredient} error={error} />;
            case 'recipes':
                return <h2>Recipes</h2>;
            case 'settings':
                return <h2>Settings</h2>;
            default:
                return <MyFridge fridge={fridge} ingredients={ingredients} onRemove={handleRemoveIngredient} onAdd={handleAddIngredient} error={error} />;
        }
    };

    return (
        <>
            <Sidebar user={user} currentView={currentView} navigateTo={setCurrentView} />

            <div className="main-content">
                <div className="content-wrapper">
                    {loading ? <p>Loading...</p> : renderContent()}
                </div>
            </div>
        </>
    );
};

export default Dashboard;