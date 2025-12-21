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

app.use('/users', userRoutes);
app.use('/recipes', recipeRoutes);

app.listen(process.env.PORT || 3000, function(req, res) {
  console.log("Server started on http://localhost:3000");
});