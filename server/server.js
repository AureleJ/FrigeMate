var express = require('express');
var bodyParser = require("body-parser");
const cors = require('cors');

const userRoutes = require('./routes/users');
const recipeRoutes = require('./routes/recipes');

var app = express();
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.json());
app.use(cors());

const checkApiKey = (req, res, next) => {
    const serverApiKey = process.env.API_KEY; 
    console.log("Server API Key:", serverApiKey);

    if (!serverApiKey) {
        console.warn("Warning: API_KEY is not set on the server. Skipping API key check.");
        return next();
    }

    const clientApiKey = req.headers['x-api-key'];
    console.log("Client API Key:", clientApiKey);


    if (!clientApiKey || clientApiKey !== serverApiKey) {
        console.error("Unauthorized access attempt with API Key:", clientApiKey);
        return res.status(401).json({ error: 'Unauthorized: Invalid API Key' });
    }

    console.log("API Key validated successfully.");

    next();
};

app.use(checkApiKey);

app.use('/users', userRoutes);
app.use('/recipes', recipeRoutes);

app.listen(process.env.PORT || 3000, function(req, res) {
  console.log("Server started on http://localhost:3000");
});