import React, { useState, useEffect } from 'react';
import RecipeModal from '../components/RecipeModal';

const Recipes = ({ recipes, userIngredients = [] }) => {
    const [sortBy, setSortBy] = useState('name');
    const [sortedRecipes, setSortedRecipes] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [selectedIngredients, setSelectedIngredients] = useState([]);
    const [selectedRecipe, setSelectedRecipe] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const recipesPerPage = 6;

    useEffect(() => {
        if (recipes) {
            let filtered = [...recipes];

            // Filter by selected ingredients
            if (selectedIngredients.length > 0) {
                filtered = filtered.filter(recipe => 
                    selectedIngredients.every(selectedIng => 
                        recipe.ingredients.some(rIng => 
                            rIng.item.toLowerCase().includes(selectedIng.toLowerCase())
                        )
                    )
                );
            }

            if (sortBy === 'name') {
                filtered.sort((a, b) => a.name.localeCompare(b.name));
            } else if (sortBy === 'prep_time') {
                filtered.sort((a, b) => a.prep_time_min - b.prep_time_min);
            } else if (sortBy === 'cook_time') {
                filtered.sort((a, b) => a.cook_time_min - b.cook_time_min);
            }
            setSortedRecipes(filtered);
            setCurrentPage(1);
        }
    }, [recipes, sortBy, selectedIngredients]);

    const toggleIngredient = (ingredientName) => {
        setSelectedIngredients(prev => 
            prev.includes(ingredientName)
                ? prev.filter(i => i !== ingredientName)
                : [...prev, ingredientName]
        );
    };

    const openRecipeModal = (recipe) => {
        console.log('Opening modal for recipe:', recipe);
        setSelectedRecipe(recipe);
        setIsModalOpen(true);
    };

    const closeRecipeModal = () => {
        console.log('Closing modal');
        setIsModalOpen(false);
        setSelectedRecipe(null);
    };

    const indexOfLastRecipe = currentPage * recipesPerPage;
    const indexOfFirstRecipe = indexOfLastRecipe - recipesPerPage;
    const currentRecipes = sortedRecipes.slice(indexOfFirstRecipe, indexOfLastRecipe);
    const totalPages = Math.ceil(sortedRecipes.length / recipesPerPage);

    const handlePrevPage = () => {
        setCurrentPage(prev => Math.max(prev - 1, 1));
    };

    const handleNextPage = () => {
        setCurrentPage(prev => Math.min(prev + 1, totalPages));
    };

    return (
        <div className="recipes-container">
            <h2 className="page-title">Recipes</h2>

            {userIngredients && userIngredients.length > 0 && (
                <div className="filter-section">
                    <h3>Filter by My Ingredients</h3>
                    <div className="ingredient-filters">
                        {userIngredients.map((ing, index) => (
                            <button 
                                key={ing.id || index} 
                                className={`filter-chip ${selectedIngredients.includes(ing.name) ? 'active' : ''}`}
                                onClick={() => toggleIngredient(ing.name)}
                            >
                                {ing.name}
                            </button>
                        ))}
                    </div>
                    {selectedIngredients.length > 0 && (
                        <button className="btn-clear-filters" onClick={() => setSelectedIngredients([])}>
                            Clear Filters
                        </button>
                    )}
                </div>
            )}

            <div className="sort-options">
                <label htmlFor="sort-select">Sort by:</label>
                <select 
                    id="sort-select" 
                    className="sort-select"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                >
                    <option value="name">Name</option>
                    <option value="prep_time">Preparation Time</option>
                    <option value="cook_time">Cooking Time</option>
                </select>
            </div>

            {currentRecipes && currentRecipes.length > 0 ? (
                <div className="recipe-list">
                    {currentRecipes.map((recipe) => (
                        <div 
                            key={recipe.id} 
                            className="recipe-item"
                            onClick={() => openRecipeModal(recipe)}
                        >
                            <div className="recipe-header">
                                <h3>{recipe.name}</h3>
                            </div>

                            <div className="recipe-description">
                                {recipe.description}
                            </div>

                            <div className="recipe-section">
                                <h4>Ingredients</h4>
                                <ul className="recipe-ingredients">
                                    {recipe.ingredients.slice(0, 3).map((ing, index) => (
                                        <li key={index}>
                                            <span className="ingredient-name">{ing.item}</span>
                                        </li>
                                    ))}
                                    {recipe.ingredients.length > 3 && (
                                        <li className="more-ingredients">+{recipe.ingredients.length - 3} more...</li>
                                    )}
                                </ul>
                            </div>

                            <div className="recipe-footer">
                                <div className="recipe-time">
                                    <span>Prep: <span className="time-value">{recipe.prep_time_min} min</span></span>
                                </div>
                                <div className="recipe-time">
                                    <span>Cook: <span className="time-value">{recipe.cook_time_min} min</span></span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state">
                    <span className="empty-icon">🍳</span>
                    <p>No recipes found matching your selected ingredients.</p>
                </div>
            )}

            {sortedRecipes.length > recipesPerPage && (
                <div className="pagination-controls">
                    <button 
                        className="btn-pagination btn-pagination-prev" 
                        onClick={handlePrevPage} 
                        disabled={currentPage === 1}
                    >
                        Previous
                    </button>
                    <span className="pagination-info">Page {currentPage} of {totalPages}</span>
                    <button 
                        className="btn-pagination btn-pagination-next" 
                        onClick={handleNextPage} 
                        disabled={currentPage === totalPages}
                    >
                        Next
                    </button>
                </div>
            )}

            <RecipeModal 
                isOpen={isModalOpen} 
                onClose={closeRecipeModal} 
                recipe={selectedRecipe} 
                userIngredients={userIngredients}
            />
        </div>
    );
}

export default Recipes;