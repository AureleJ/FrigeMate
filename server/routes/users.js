const express = require('express');
const router = express.Router();
const users = require('../db/users');
const fridges = require('../db/fridges');
const ingredients = require('../db/ingredients');

/*--- User Routes ---*/

router.get('/', async function(req, res) {
  try {
    const allUsers = await users.getUsers();
    res.send(allUsers);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.get('/:userId', async function(req, res) {
  try {
    const user = await users.getUserById(req.params.userId);
    res.send(user);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/', async function(req, res) {
  try {
    const { username } = req.body;
    if (!username) {
      return res.status(400).send({ error: 'username requis' });
    }
    const newUser = await users.createUser(username);
    res.status(201).send(newUser);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.put('/:userId', async function(req, res) {
  try {
    const { username } = req.body;
    await users.updateUser(req.params.userId, username);
    res.send({ message: 'User updated' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.delete('/:userId', async function(req, res) {
  try {
    await users.deleteUser(req.params.userId);
    res.send({ message: 'User deleted' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/login', async function(req, res) {
  try {
    const { username } = req.body;
    if (!username) {
      return res.status(400).send({ error: 'username requis' });
    }
    const user = await users.loginUser(username);
    res.send({ success: true, user: user });
  } catch (err) {
    console.error('Login error:', err.message);
    res.status(401).send({ error: err.message });
  }
});

/*--- Fridge Routes ---*/

// Get user's single fridge
router.get('/:userId/fridge', async function(req, res) {
  try {
    const userFridge = await fridges.getFridgeByUserId(req.params.userId);
    res.send({ fridge: userFridge });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

// Update user's fridge
router.put('/:userId/fridge', async function(req, res) {
  try {
    const { name, description } = req.body;
    if (!name) {
      return res.status(400).send({ error: 'name required' });
    }
    await fridges.updateFridge(req.params.userId, name, description || '');
    res.send({ message: 'Fridge updated' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

/*--- Ingredient Routes ---*/

// Get ingredients from user's fridge
router.get('/:userId/fridge/ingredients', async function(req, res) {
  try {
    const userFridge = await fridges.getFridgeByUserId(req.params.userId);
    const fridgeIngredients = await ingredients.getIngredientsByFridgeId(userFridge.id);
    res.send({ ingredients: fridgeIngredients });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

// Add ingredient to user's fridge
router.post('/:userId/fridge/ingredients', async function(req, res) {
  try {
    console.log('Request body for adding ingredient:', req.body);
    const { name, quantity, unit, expiryDate, category } = req.body;
    if (!name) {
      return res.status(400).send({ error: 'name required' });
    }
    const userFridge = await fridges.getFridgeByUserId(req.params.userId);
    const newIngredient = await ingredients.addIngredient(
      userFridge.id,
      name,
      quantity || 0,
      unit || '',
      expiryDate || null,
      category || ''
    );
    res.status(201).send(newIngredient);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

// Update ingredient in user's fridge
router.put('/:userId/fridge/ingredients/:ingredientId', async function(req, res) {
  try {
    const { name, quantity, unit, expiryDate, category } = req.body;
    await ingredients.updateIngredient(req.params.ingredientId, name, quantity, unit, expiryDate, category);
    res.send({ message: 'Ingredient updated' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

// Delete ingredient from user's fridge
router.delete('/:userId/fridge/ingredients/:ingredientId', async function(req, res) {
  try {
    await ingredients.deleteIngredient(req.params.ingredientId);
    res.send({ message: 'Ingredient deleted' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

module.exports = router;