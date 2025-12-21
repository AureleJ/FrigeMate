const fs = require('fs');

function getRecipes() {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) reject(err);
            else resolve(JSON.parse(data));
        });
    });
};

function getRecipeById(id) {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) reject(err);
            else {
                const recipesData = JSON.parse(data);
                const recipes = recipesData.recipes || [];
                const recipe = recipes.find(r => r.id == id);
                if (recipe) resolve(recipe);
                else reject(new Error('Recipe not found'));
            }
        });
    });
};

function getRecipeByIngredients(ingredients) {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) reject(err);
            else {
                const recipesData = JSON.parse(data);
                const recipes = recipesData.recipes || [];
                // Filter recipes that contain ALL selected ingredients
                const filteredRecipes = recipes.filter(r => 
                    ingredients.every(ingName => 
                        r.ingredients.some(rIng => rIng.item.toLowerCase().includes(ingName.toLowerCase()))
                    )
                );
                resolve(filteredRecipes);
            }
        });
    });
};

function addRecipe(recipe) {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) return reject(err);
            const recipesData = JSON.parse(data);
            const recipes = recipesData.recipes || [];
            
            // Generate new ID
            const maxId = recipes.reduce((max, r) => Math.max(max, r.id), 0);
            recipe.id = maxId + 1;
            
            recipes.push(recipe);
            recipesData.recipes = recipes;
            
            fs.writeFile('./data/recipes.json', JSON.stringify(recipesData, null, 2), (err) => {
                if (err) reject(err);
                else resolve(recipe);
            });
        });
    });
};

function updateRecipe(id, recipe) {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) return reject(err);
            let recipesData = JSON.parse(data);
            let recipes = recipesData.recipes || [];
            
            const index = recipes.findIndex(r => r.id == id);
            if (index === -1) return reject(new Error('Recipe not found'));
            
            recipes[index] = { ...recipes[index], ...recipe };
            recipesData.recipes = recipes;
            
            fs.writeFile('./data/recipes.json', JSON.stringify(recipesData, null, 2), (err) => {
                if (err) reject(err);
                else resolve(recipes[index]);
            });
        });
    });
};

function deleteRecipe(id) {
    return new Promise((resolve, reject) => {
        fs.readFile('./data/recipes.json', 'utf8', (err, data) => {
            if (err) return reject(err);
            let recipesData = JSON.parse(data);
            let recipes = recipesData.recipes || [];
            
            const index = recipes.findIndex(r => r.id == id);
            if (index === -1) return reject(new Error('Recipe not found'));
            
            recipes.splice(index, 1);
            recipesData.recipes = recipes;
            
            fs.writeFile('./data/recipes.json', JSON.stringify(recipesData, null, 2), (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    });
};

module.exports = {
    getRecipes,
    getRecipeById,
    getRecipeByIngredients,
    addRecipe,
    updateRecipe,
    deleteRecipe
};