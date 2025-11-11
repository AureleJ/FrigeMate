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
    const { username, email, password } = req.body;
    if (!username || !email || !password) {
      return res.status(400).send({ error: 'username, email et password requis' });
    }
    const newUser = await users.createUser(username, email, password);
    res.status(201).send(newUser);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.put('/:userId', async function(req, res) {
  try {
    const { username, email } = req.body;
    await users.updateUser(req.params.userId, username, email);
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
    res.status(401).send({ error: err.message });
  }
});

/*--- Fridge Routes ---*/

router.get('/:userId/fridges', async function(req, res) {
  try {
    const userFridges = await fridges.getFridgesByUserId(req.params.userId);
    res.send({ fridges: userFridges });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/:userId/fridges', async function(req, res) {
  try {
    const { name, description } = req.body;
    if (!name) {
      return res.status(400).send({ error: 'name requis' });
    }
    const newFridge = await fridges.createFridge(req.params.userId, name, description || '');
    res.status(201).send(newFridge);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.get('/:userId/fridges/:fridgeId', async function(req, res) {
  try {
    const fridge = await fridges.getFridgeById(req.params.fridgeId);
    res.send(fridge);
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.put('/:userId/fridges/:fridgeId', async function(req, res) {
  try {
    const { name, description } = req.body;
    await fridges.updateFridge(req.params.fridgeId, name, description);
    res.send({ message: 'Fridge updated' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.delete('/:userId/fridges/:fridgeId', async function(req, res) {
  try {
    await fridges.deleteFridge(req.params.fridgeId);
    res.send({ message: 'Fridge deleted' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

/*--- Ingredient Routes ---*/

router.get('/:userId/fridges/:fridgeId/ingredients', async function(req, res) {
  try {
    const fridgeIngredients = await ingredients.getIngredientsByFridgeId(req.params.fridgeId);
    res.send({ ingredients: fridgeIngredients });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.post('/:userId/fridges/:fridgeId/ingredients', async function(req, res) {
  try {
    const { name, quantity, unit, expiryDate, category } = req.body;
    if (!name) {
      return res.status(400).send({ error: 'name requis' });
    }
    const newIngredient = await ingredients.addIngredient(
      req.params.fridgeId,
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

router.put('/:userId/fridges/:fridgeId/ingredients/:ingredientId', async function(req, res) {
  try {
    const { name, quantity, unit, expiryDate, category } = req.body;
    await ingredients.updateIngredient(req.params.ingredientId, name, quantity, unit, expiryDate, category);
    res.send({ message: 'Ingredient updated' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

router.delete('/:userId/fridges/:fridgeId/ingredients/:ingredientId', async function(req, res) {
  try {
    await ingredients.deleteIngredient(req.params.ingredientId);
    res.send({ message: 'Ingredient deleted' });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

module.exports = router;
