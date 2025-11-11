const db = require('../db/db-init');

function getFridgesByUserId(userId) {
    return new Promise((resolve, reject) => {
        db.all('SELECT * FROM fridges WHERE userId = ?', [userId], (err, rows) => {
            if (err) reject(err);
            else resolve(rows);
        });
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

function createFridge(userId, name, description) {
    return new Promise((resolve, reject) => {
        db.run(
            'INSERT INTO fridges (userId, name, description) VALUES (?, ?, ?)',
            [userId, name, description],
            function(err) {
                if (err) reject(err);
                else resolve({ id: this.lastID, userId, name, description });
            }
        );
    });
}

function updateFridge(id, name, description) {
    return new Promise((resolve, reject) => {
        db.run(
            'UPDATE fridges SET name = ?, description = ? WHERE id = ?',
            [name, description, id],
            (err) => {
                if (err) reject(err);
                else resolve();
            }
        );
    });
}

function deleteFridge(id) {
    return new Promise((resolve, reject) => {
        db.run('DELETE FROM fridges WHERE id = ?', [id], (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

module.exports = {
    getFridgesByUserId,
    getFridgeById,
    createFridge,
    updateFridge,
    deleteFridge
};
