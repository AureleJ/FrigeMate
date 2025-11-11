const db = require('../db/db-init');

function getIngredientsByFridgeId(fridgeId) {
    return new Promise((resolve, reject) => {
        db.all('SELECT * FROM ingredients WHERE fridgeId = ?', [fridgeId], (err, rows) => {
            if (err) reject(err);
            else resolve(rows);
        });
    });
}

function getIngredientById(id) {
    return new Promise((resolve, reject) => {
        db.get('SELECT * FROM ingredients WHERE id = ?', [id], (err, row) => {
            if (err) reject(err);
            else resolve(row);
        });
    });
}

function addIngredient(fridgeId, name, quantity, unit, expiryDate, category) {
    return new Promise((resolve, reject) => {
        db.run(
            'INSERT INTO ingredients (fridgeId, name, quantity, unit, expiryDate, category) VALUES (?, ?, ?, ?, ?, ?)',
            [fridgeId, name, quantity, unit, expiryDate, category],
            function(err) {
                if (err) reject(err);
                else resolve({ id: this.lastID, fridgeId, name, quantity, unit, expiryDate, category });
            }
        );
    });
}

function updateIngredient(id, name, quantity, unit, expiryDate, category) {
    return new Promise((resolve, reject) => {
        db.run(
            'UPDATE ingredients SET name = ?, quantity = ?, unit = ?, expiryDate = ?, category = ? WHERE id = ?',
            [name, quantity, unit, expiryDate, category, id],
            (err) => {
                if (err) reject(err);
                else resolve();
            }
        );
    });
}

function deleteIngredient(id) {
    return new Promise((resolve, reject) => {
        db.run('DELETE FROM ingredients WHERE id = ?', [id], (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

module.exports = {
    getIngredientsByFridgeId,
    getIngredientById,
    addIngredient,
    updateIngredient,
    deleteIngredient
};
