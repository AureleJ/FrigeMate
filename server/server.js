var express = require('express');
var bodyParser = require("body-parser");
const cors = require('cors');

const users = require('./routes/users');
const fridges = require('./routes/fridges');
const ingredients = require('./routes/ingredients');

var app = express();
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors());

// =====================
// USER ROUTES
// =====================

// GET all users
app.get("/users", async function(req, res) {
    try {
        const allUsers = await users.getUsers();
        res.send(allUsers);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// GET a user by ID
app.get("/user/:id", async function(req, res) {
    try {
        const user = await users.getUserById(req.params.id);
        res.send(user);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// POST create a new user
app.post("/user", async function(req, res) {
    try {
        const { username, email, password } = req.body;
        if (!username || !email || !password) {
            return res.status(400).send({ error: "username, email et password requis" });
        }
        const newUser = await users.createUser(username, email, password);
        res.status(201).send(newUser);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// PUT update a user
app.put("/user/:id", async function(req, res) {
    try {
        const { username, email } = req.body;
        await users.updateUser(req.params.id, username, email);
        res.send({ message: "User updated" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// DELETE delete a user
app.delete("/user/:id", async function(req, res) {
    try {
        await users.deleteUser(req.params.id);
        res.send({ message: "User deleted" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// POST login with username
app.post("/login", async function(req, res) {
    try {
        const { username } = req.body;
        if (!username) {
            return res.status(400).send({ error: "username requis" });
        }
        const user = await users.loginUser(username);
        res.send({ success: true, user: user });
    } catch (err) {
        res.status(401).send({ error: err.message });
    }
});

// =====================
// FRIDGE ROUTES
// =====================

// GET user fridges
app.get("/user/:userId/fridges", async function(req, res) {
    try {
        const userFridges = await fridges.getFridgesByUserId(req.params.userId);
        res.send(userFridges);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// GET a fridge by ID
app.get("/fridge/:id", async function(req, res) {
    try {
        const fridge = await fridges.getFridgeById(req.params.id);
        res.send(fridge);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// POST create a new fridge
app.post("/fridge", async function(req, res) {
    try {
        const { userId, name, description } = req.body;
        if (!userId || !name) {
            return res.status(400).send({ error: "userId et name requis" });
        }
        const newFridge = await fridges.createFridge(userId, name, description || "");
        res.status(201).send(newFridge);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// PUT update a fridge
app.put("/fridge/:id", async function(req, res) {
    try {
        const { name, description } = req.body;
        await fridges.updateFridge(req.params.id, name, description);
        res.send({ message: "Fridge updated" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// DELETE delete a fridge
app.delete("/fridge/:id", async function(req, res) {
    try {
        await fridges.deleteFridge(req.params.id);
        res.send({ message: "Fridge deleted" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// =====================
// INGREDIENT ROUTES
// =====================

// GET fridge ingredients
app.get("/fridge/:fridgeId/ingredients", async function(req, res) {
    try {
        const fridgeIngredients = await ingredients.getIngredientsByFridgeId(req.params.fridgeId);
        res.send(fridgeIngredients);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// GET an ingredient by ID
app.get("/ingredient/:id", async function(req, res) {
    try {
        const ingredient = await ingredients.getIngredientById(req.params.id);
        res.send(ingredient);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// POST create a new ingredient
app.post("/ingredient", async function(req, res) {
    try {
        const { fridgeId, name, quantity, unit, expiryDate, category } = req.body;
        if (!fridgeId || !name) {
            return res.status(400).send({ error: "fridgeId et name requis" });
        }
        const newIngredient = await ingredients.addIngredient(
            fridgeId, 
            name, 
            quantity || 0, 
            unit || "", 
            expiryDate || null, 
            category || ""
        );
        res.status(201).send(newIngredient);
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// PUT update an ingredient
app.put("/ingredient/:id", async function(req, res) {
    try {
        const { name, quantity, unit, expiryDate, category } = req.body;
        await ingredients.updateIngredient(req.params.id, name, quantity, unit, expiryDate, category);
        res.send({ message: "Ingredient updated" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

// DELETE delete an ingredient
app.delete("/ingredient/:id", async function(req, res) {
    try {
        await ingredients.deleteIngredient(req.params.id);
        res.send({ message: "Ingredient deleted" });
    } catch (err) {
        res.status(500).send({ error: err.message });
    }
});

app.listen(process.env.PORT || 3000, function(req, res) {
    console.log("Server started on http://localhost:3000");
});