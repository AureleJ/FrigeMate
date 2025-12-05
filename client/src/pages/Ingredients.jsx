import React, { useState } from 'react';
import IngredientList from '../components/IngredientList';
import AddIngredientForm from '../components/AddIngredientForm';


const Ingredients = ({ ingredients, fridge, error, onAdd, onRemove }) => {
    const [isAddingIngredient, setIsAddingIngredient] = React.useState(false);

    return (
        <>
            <IngredientList ingredients={ingredients} onRemove={onRemove} />

            {isAddingIngredient && (
                <AddIngredientForm
                    onAdd={(ingredient) => {
                        onAdd(ingredient);
                        setIsAddingIngredient(false);
                    }}
                    onClosePopup={() => setIsAddingIngredient(false)}
                />
            )}

            <button className="btn-add-ingredient btn btn-primary" style={{ flexShrink: 0 }} onClick={() => setIsAddingIngredient(true)}>Add Ingredient</button>
        </>
    );
}

export default Ingredients;