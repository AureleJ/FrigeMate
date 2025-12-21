const express = require('express');
const router = express.Router();
const ingredients = require('../db/recipe.js');

/*--- Recipe Routes ---*/
router.get('/', async function(req, res) {
  try {
    const allRecipes = await ingredients.getRecipes();
    res.send(allRecipes);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.get('/:recipeId', async function(req, res) {
  try {
    const recipe = await ingredients.getRecipeById(req.params.recipeId);
    res.send(recipe);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/search', async function(req, res) {
  try {
    const { ingredients: ingredientList } = req.body;
    const recipes = await ingredients.getRecipeByIngredients(ingredientList);
    res.send(recipes);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/', async function(req, res) {
    try {
        const recipe = req.body;
        const newRecipe = await ingredients.addRecipe(recipe);
        res.status(201).send(newRecipe);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

router.put('/:recipeId', async function(req, res) {
    try {
        const recipe = req.body;
        await ingredients.updateRecipe(req.params.recipeId, recipe);
        res.send({ message: 'Recipe updated' });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

router.delete('/:recipeId', async function(req, res) {
    try {
        await ingredients.deleteRecipe(req.params.recipeId);
        res.send({ message: 'Recipe deleted' });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

module.exports = router;