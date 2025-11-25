const db = require('../db/db-init');

function getFridgeByUserId(userId) {
    return new Promise((resolve, reject) => {
        db.get('SELECT * FROM fridges WHERE userId = ?', [userId], (err, row) => {
            if (err) {
                reject(err);
            } else if (row) {
                resolve(row);
            } else {
                createDefaultFridge(userId)
                    .then(fridge => resolve(fridge))
                    .catch(err => reject(err));
            }
        });
    });
}

function createDefaultFridge(userId) {
    return new Promise((resolve, reject) => {
        const defaultName = 'My Fridge';
        const defaultDescription = 'Your personal fridge';
        
        db.run(
            'INSERT INTO fridges (userId, name, description) VALUES (?, ?, ?)',
            [userId, defaultName, defaultDescription],
            function(err) {
                if (err) reject(err);
                else resolve({ id: this.lastID, userId, name: defaultName, description: defaultDescription });
            }
        );
    });
}

function updateFridge(userId, name, description) {
    return new Promise((resolve, reject) => {
        db.run(
            'UPDATE fridges SET name = ?, description = ? WHERE userId = ?',
            [name, description, userId],
            (err) => {
                if (err) reject(err);
                else resolve();
            }
        );
    });
}

function getFridgeById(id) {
    return new Promise((resolve, reject) => {
        db.get('SELECT * FROM fridges WHERE id = ?', [id], (err, row) => {
            if (err) reject(err);
            else resolve(row);
        });
    });
}

module.exports = {
    getFridgeByUserId,
    createDefaultFridge,
    updateFridge,
    getFridgeById
};
