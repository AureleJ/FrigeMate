import React, { useState, useEffect } from 'react';
import { authService } from '../services/auth';
import Sidebar from '../components/Slidebar';
import MyFridge from './MyFridge';
import Ingredients from './Ingredients';
import Loading from './Loading';
import Recipes from './Recipes';
import { apiService } from '../services/api';

const Dashboard = () => {
    const user = authService.getCurrentUser();
    const [currentView, setCurrentView] = useState('fridge');
    const [ingredients, setIngredients] = useState([]);
    const [fridge, setFridge] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [recipes, setRecipes] = useState([]);
    const [recipeOfTheDay, setRecipeOfTheDayState] = useState(null);

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

    const loadRecipes = async () => {
        try {
            const data = await apiService.getRecipes();
            setRecipes(data.recipes || []);
            console.log('Loaded recipes data:', data);
        } catch (err) {
            setError('Failed to load recipes');
            console.error(err);
        }
    };

    useEffect(() => {
        if (recipes.length > 0 && ingredients.length > 0) {
            const sortedIngredients = [...ingredients]
                .filter(ing => ing.expiryDate >= new Date().toISOString())
                .sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate));
            console.log('Sorted ingredients by expiry date:', sortedIngredients);

            let foundRecipe = null;

            for (const ingredient of sortedIngredients) {
                const ingredientName = ingredient.name.toLowerCase();

                foundRecipe = recipes.find(recipe =>
                    recipe.ingredients.some(rIng =>
                        rIng.item.toLowerCase().includes(ingredientName)
                    )
                );
                
                if (foundRecipe) break;
            }

            setRecipeOfTheDayState(foundRecipe || null);
        }
    }, [recipes, ingredients]);

    useEffect(() => {
        const loadData = async () => {
            setLoading(true);
            await loadFridge();
            await loadIngredients();
            await loadRecipes();
            setLoading(false);
        };

        loadData();
    }, []);

    const renderContent = () => {
        switch (currentView) {
            case 'fridge':
                return <MyFridge fridge={fridge} ingredients={ingredients} onRemove={handleRemoveIngredient} error={error} recipeOfTheDay={recipeOfTheDay} />;
            case 'ingredients':
                return <Ingredients ingredients={ingredients} onRemove={handleRemoveIngredient} onAdd={handleAddIngredient} error={error} />;
            case 'recipes':
                return <Recipes recipes={recipes} userIngredients={ingredients} />;
            case 'settings':
                return <h2>Settings</h2>;
            default:
                return <MyFridge fridge={fridge} ingredients={ingredients} onRemove={handleRemoveIngredient} error={error} recipeOfTheDay={recipeOfTheDay} />;
        }
    };

    if (loading) {
        return <Loading />;
    }

    return (
        <>
            <Sidebar user={user} currentView={currentView} navigateTo={setCurrentView} />

            <div className="main-content">
                <div className="content-wrapper">
                    {renderContent()}
                </div>
            </div>
        </>
    );
};

export default Dashboard;