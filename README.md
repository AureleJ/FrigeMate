# FrigeMate

> Smart fridge manager - track ingredients, get expiry alerts, and discover recipes based on what you have.

**[Live Demo](https://fridgemate-client.vercel.app)**

![Status](https://img.shields.io/badge/status-in%20progress-yellow)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Node.js](https://img.shields.io/badge/Node.js-Express-339933?logo=node.js)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite)
![Deployed on Vercel](https://img.shields.io/badge/Deployed-Vercel-black?logo=vercel)

---

## Overview

FrigeMate is a full-stack web app that helps you manage your fridge inventory, reduce food waste, and plan meals efficiently.

The app smartly suggests a **Recipe of the Day** based on the ingredients in your fridge that are closest to expiring - so nothing goes to waste.

> Built during my exchange semester at Tamkang University, Taiwan.

---

## Features

- **Authentication** - Username-based login and registration, secured with API key middleware
- **Ingredient Management** - Add, edit, and delete ingredients with quantity, unit, category, and expiry date
- **Expiry Tracking** - Visual alerts for items expiring soon or already expired
- **Recipe of the Day** - Automatically suggests a recipe based on your soonest-expiring ingredients
- **Recipe Browser** - Browse, filter by your ingredients, and sort by name or cooking time
- **Fridge Overview** - Dashboard with stats (total items, expiring soon, expired) and a category pie chart
- **Electron Support** - Can run as a desktop app via Electron

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Recharts |
| Backend | Node.js, Express 5 |
| Database | SQLite3 |
| Auth | API Key middleware + username-based session |
| Desktop | Electron |
| Deployment | Vercel (client) |

---

## Architecture

```
FrigeMate/
├── client/                  # React frontend (Vite)
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   │   ├── AddIngredientForm.jsx
│   │   │   ├── IngredientItem.jsx
│   │   │   ├── IngredientList.jsx
│   │   │   ├── RecipeModal.jsx
│   │   │   └── Slidebar.jsx
│   │   ├── pages/           # App views
│   │   │   ├── Dashboard.jsx
│   │   │   ├── MyFridge.jsx
│   │   │   ├── Ingredients.jsx
│   │   │   ├── Recipes.jsx
│   │   │   ├── Login.jsx
│   │   │   └── Loading.jsx
│   │   └── services/        # API & auth layer
│   │       ├── api.js
│   │       └── auth.js
│   └── electron/            # Desktop wrapper
└── server/                  # Node.js backend
    ├── routes/
    │   ├── users.js         # Users, fridge & ingredients routes
    │   └── recipes.js       # Recipe routes
    ├── db/
    │   ├── db-init.js       # SQLite schema
    │   ├── users.js
    │   ├── fridges.js
    │   ├── ingredients.js
    │   └── recipe.js
    └── server.js
```

---

## Database Schema

```
users         → id, username, createdAt
fridges       → id, userId (1:1), name, description
ingredients   → id, fridgeId, name, quantity, unit, expiryDate, category
recipes       → loaded from recipes.json
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/users/login` | Login with username |
| POST | `/users` | Register new user |
| GET | `/users/:id/fridge` | Get user's fridge |
| GET | `/users/:id/fridge/ingredients` | Get all ingredients |
| POST | `/users/:id/fridge/ingredients` | Add ingredient |
| PUT | `/users/:id/fridge/ingredients/:id` | Update ingredient |
| DELETE | `/users/:id/fridge/ingredients/:id` | Delete ingredient |
| GET | `/recipes` | Get all recipes |
| POST | `/recipes/search` | Search recipes by ingredients |

All routes are protected by an `x-api-key` header.

---

## Getting Started

### Prerequisites

- Node.js 18+
- npm

### Installation

```bash
# Clone the repo
git clone https://github.com/AureleJ/FrigeMate.git
cd FrigeMate
```

### Environment Variables

Create a `.env` file in the `server/` directory:

```env
PORT=3000
API_KEY=your_secret_api_key
```

Create a `.env` file in the `client/` directory:

```env
VITE_API_URL=http://localhost:3000
VITE_API_KEY=your_secret_api_key
```

### Run the app

```bash
# Start the server
cd server && npm install && npm start

# Start the client (new terminal)
cd client && npm install && npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

### Run as desktop app (Electron)

```bash
cd client && npm run electron:dev
```

---

## Author

**Aurèle** - Engineering student at ESIEA Laval.
Built during exchange semester at Tamkang University, Taiwan
