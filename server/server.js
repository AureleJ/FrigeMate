var express = require('express');
var bodyParser = require("body-parser");
const cors = require('cors');

const apiRouter = require('./routes/api');

var app = express();
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors());

app.use('/users', apiRouter);

app.post('/login', async function(req, res) {
  try {
    const users = require('./db/users');
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

app.listen(process.env.PORT || 3000, function(req, res) {
  console.log("Server started on http://localhost:3000");
});