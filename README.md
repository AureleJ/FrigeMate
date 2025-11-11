# FridgeMate

FridgeMate is a smart web application designed to help users **manage their food inventory** efficiently and **reduce food waste**.  
Users can add ingredients manually or by **scanning barcodes**, track expiration dates, and receive **recipe suggestions** based on what they have.  
The goal is to simplify meal planning, save time, and promote sustainable consumption habits.

---

## Context & Problem Solved
Every day, large amounts of food are wasted because:
- Households forget what’s in their fridge.
- Items expire before being used.

FridgeMate solves this by:
- **Tracking stored ingredients automatically**.
- **Alerting users before food expires**.
- **Suggesting recipes** to use ingredients efficiently.

This project is inspired by Taiwanese culture, where food waste is strongly discouraged and leftovers are valued.

---

## Key Features
- **Ingredient Management**: Add items manually or via barcode scanning.  
- **Expiration Tracking**: Automatic reminders for items nearing expiration.  
- **Recipe Suggestions**: Based on available ingredients and user preferences.  
- **Smart Alerts**: Highlight items to consume soon and suggest recipes using them first.  
- **Meal Planning**: Plan meals in advance and generate grocery lists.  
- **Search & Filter**: Find recipes by ingredient, category, or dietary preference.  

---

## Tech Stack
- **Frontend**: HTML, CSS, JavaScript  
- **Backend**: Node.js (Express)  
- **Database**: `localStorage` for ingredient persistence and `SQLite` 
- **External APIs**:
  - **Recipe API**: Fetch recipes based on ingredient lists.
  - **AI Integration (Gemini)**: Enhanced recipe suggestions and user interactions.
- **Barcode Scanning**: JavaScript-based QR/barcode scanner library for web & mobile.  
- **Data Handling**:
  - Ingredients, expiration dates, and preferences stored locally.
  - Recipes cached to reduce network usage.  

---

## Goal
FridgeMate aims to:
- **Simplify meal planning**  
- **Reduce food waste**  
- **Promote sustainable habits**  
- **Save time and money**  
