const db = require('../db/db-init');

function getUsers() {
    return new Promise((resolve, reject) => {
        db.all('SELECT id, username, createdAt FROM users', (err, rows) => {
            if (err) reject(err);
            else resolve(rows);
        });
    });
}

function getUserById(id) {
    return new Promise((resolve, reject) => {
        db.get('SELECT id, username, createdAt FROM users WHERE id = ?', [id], (err, row) => {
            if (err) reject(err);
            else resolve(row);
        });
    });
}

function createUser(username) {
    return new Promise((resolve, reject) => {
        db.run(
            'INSERT INTO users (username) VALUES (?)',
            [username],
            function(err) {
                if (err) reject(err);
                else resolve({ id: this.lastID, username });
            }
        );
    });
}

function updateUser(id, username) {
    return new Promise((resolve, reject) => {
        db.run(
            'UPDATE users SET username = ? WHERE id = ?',
            [username, id],
            (err) => {
                if (err) reject(err);
                else resolve();
            }
        );
    });
}

function deleteUser(id) {
    return new Promise((resolve, reject) => {
        db.run('DELETE FROM users WHERE id = ?', [id], (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

function loginUser(username) {
    return new Promise((resolve, reject) => {
        db.get('SELECT id, username, createdAt FROM users WHERE username = ?', [username], (err, row) => {
            if (err) reject(err);
            else if (!row) reject(new Error('Utilisateur non trouvé'));
            else resolve(row);
        });
    });
}

module.exports = {
    getUsers,
    getUserById,
    createUser,
    updateUser,
    deleteUser,
    loginUser
};