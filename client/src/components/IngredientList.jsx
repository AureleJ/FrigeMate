import React from 'react';
import IngredientItem from './IngredientItem';

const IngredientList = ({type, sortBy, maxElements, ingredients, onRemove }) => {
    if (!ingredients || ingredients.length === 0) {
        return (
            <div className="empty-state">
                <div className="empty-icon">🥗</div>
                <h3>No ingredients yet</h3>
                <p>Add your first ingredient to start managing your fridge!</p>
            </div>
        );
    }

    if (sortBy === "expiryDate") {
        ingredients = [...ingredients].sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate));
    }
    if (sortBy === "name") {
        ingredients = [...ingredients].sort((a, b) => a.name.localeCompare(b.name));
    }
    if (sortBy === "quantity") {
        ingredients = [...ingredients].sort((a, b) => a.quantity - b.quantity);
    }
    if (sortBy === "category") {
        ingredients = [...ingredients].sort((a, b) => a.category.localeCompare(b.category));
    }

    if (!maxElements) {
        maxElements = ingredients.length;
    }

    return (
        <div className="ingredients-container">
            {ingredients.slice(0, maxElements).map(ingredient => (
                <IngredientItem 
                    key={ingredient.id} 
                    ingredient={ingredient} 
                    type={type}
                    onRemove={onRemove} 
                />
            ))}
        </div>
    );
};

export default IngredientList;
