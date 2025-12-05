const sqlite3 = require('sqlite3');
const db = new sqlite3.Database('./data/fridgemate.db');

db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    `);

    db.run(`
        CREATE TABLE IF NOT EXISTS fridges (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            userId INTEGER UNIQUE NOT NULL,
            name TEXT NOT NULL DEFAULT 'My Fridge',
            description TEXT,
            createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
        )
    `);

    db.run(`
        CREATE TABLE IF NOT EXISTS ingredients (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            fridgeId INTEGER NOT NULL,
            name TEXT NOT NULL,
            quantity REAL,
            unit TEXT,
            expiryDate DATE,
            category TEXT,
            image TEXT,
            createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (fridgeId) REFERENCES fridges(id) ON DELETE CASCADE
        )
    `);

    console.log('✅ Database initialized');
});

module.exports = db;
