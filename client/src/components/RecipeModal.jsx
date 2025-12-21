import React from 'react';

const RecipeModal = ({ isOpen, recipe, userIngredients, onClose }) => {
    if (!isOpen || !recipe) return null;

    const getMissingIngredients = () => {
        if (!userIngredients) return recipe.ingredients;
        
        return recipe.ingredients.filter(rIng => 
            !userIngredients.some(uIng => 
                rIng.item.toLowerCase().includes(uIng.name.toLowerCase())
            )
        );
    };

    const missingIngredients = getMissingIngredients();
    const hasAllIngredients = missingIngredients.length === 0;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <button className="modal-close" onClick={onClose}>&times;</button>
                
                <div className="modal-header">
                    <h2>{recipe.name}</h2>
                    <div className="modal-meta">
                        <span className="meta-tag">
                            {recipe.prep_time_min} min prep
                        </span>
                        <span className="meta-tag">
                            {recipe.cook_time_min} min cook
                        </span>
                    </div>
                </div>

                <div className="modal-body">
                    <p className="modal-description">{recipe.description}</p>

                    <div className="modal-section">
                        <h3>Ingredients</h3>
                        <div className="ingredients-status">
                            {hasAllIngredients ? (
                                <div className="status-success">
                                    <span className="icon">✅</span> You have all ingredients!
                                </div>
                            ) : (
                                <div className="status-warning">
                                    <span className="icon">🛒</span> You are missing {missingIngredients.length} ingredients
                                </div>
                            )}
                        </div>
                        
                        <ul className="modal-ingredients-list">
                            {recipe.ingredients.map((ing, index) => {
                                const isMissing = missingIngredients.includes(ing);
                                return (
                                    <li key={index} className={isMissing ? 'ingredient-missing' : 'ingredient-present'}>
                                        <span className="ing-status">{isMissing ? '❌' : '✅'}</span>
                                        <span className="ing-name">{ing.item}</span>
                                        <span className="ing-amount">{ing.quantity} {ing.unit}</span>
                                    </li>
                                );
                            })}
                        </ul>
                    </div>

                    <div className="modal-section">
                        <h3>Instructions</h3>
                        <ol className="modal-instructions-list">
                            {Array.isArray(recipe.instructions) ? (
                                recipe.instructions.map((step, index) => (
                                    <li key={index}>{step}</li>
                                ))
                            ) : (
                                <li className="single-instruction">{recipe.instructions}</li>
                            )}
                        </ol>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RecipeModal;
