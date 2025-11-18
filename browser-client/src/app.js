import { authService } from './services/auth.js';
import { apiService } from './services/api.js';

class IngredientComponent {
    constructor() {
        this.ingredients = [];
        this.container = null;
    }

    async init() {
        if (this.ingredients.length === 0) {
            this.container = `
                <div class="empty-state">
                    <div class="empty-icon">🥗</div>
                    <h3>No ingredients yet</h3>
                    <p>Add your first ingredient to start managing your fridge!</p>
                </div>
            `;
        }

        this.container = this.ingredients.map(ingredient => `
            <div class="ingredient-item" data-id="${ingredient.id}" style="background-color: ${ingredient.color}">
            <div class="ingredient-info">
                <span class="ingredient-name">${ingredient.name}</span>
                <span class="ingredient-quantity">${ingredient.quantity} ${ingredient.unit}</span>
                <span class="ingredient-expiry">Expires on: ${ingredient.expiryDate}</span>
                <span class="ingredient-category">Category: ${ingredient.category}</span>
            </div>
            <button class="btn-remove" onclick="app.removeIngredient(${ingredient.id})">🗑️</button>
            </div>
        `).join('');

        this.eventListeners();
    }

    colorCodeExpiryDates() {
        const today = new Date();

        const expiredColor = '#ffcccc'; 
        const defaultColor = '#ecf0f1'; 

        this.ingredients.forEach(ingredient => {
            const expiryDate = new Date(ingredient.expiryDate);
            if (expiryDate < today) {
                ingredient.color = expiredColor;
            } else {
                ingredient.color = defaultColor;
            }
        });
    }

    setIngredients(ingredients) {
        this.ingredients = ingredients || [];
        this.colorCodeExpiryDates();
    }

    async add(name, quantity, unit, expiryDate, category) {
        if (!name?.trim()) return false;

        try {
            await apiService.addIngredient({
                name: name.trim(),
                quantity: parseFloat(quantity) || 0,
                unit: unit?.trim() || '',
                expiryDate: expiryDate?.trim() || '',
                category: category?.trim() || ''
            });
            return true;
        } catch (error) {
            console.error('Error adding ingredient:', error);
            return false;
        }
    }

    async remove(ingredientId) {
        if (!confirm('Remove this ingredient?')) return false;

        try {
            await apiService.removeIngredient(ingredientId);
            return true;
        } catch (error) {
            console.error('Error removing ingredient:', error);
            return false;
        }
    }

    eventListeners() {
        const form = document.getElementById('addIngredientForm');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const formData = new FormData(e.target);
                const success = await this.add(
                    formData.get('name'),
                    formData.get('quantity'),
                    formData.get('unit'),
                    formData.get('expiry'),
                    formData.get('category')
                );
                if (success) {
                    e.target.reset();
                    if (window.app && window.app.fridgeComponent) {
                        await window.app.fridgeComponent.loadIngredients();
                    }
                }
            });
        }
    }
}

class FridgeComponent {
    constructor() {
        this.fridge = null;
        this.container = null;
        this.ingredientComponent = new IngredientComponent();
    }

    async init() {
        await this.loadFridge();

        await this.ingredientComponent.init();

        this.container = `
            <section class="fridge-section">
                <div class="section-header">
                    <h2 id="fridgeTitle">${this.fridge.name}</h2>
                    <button id="editFridgeBtn" class="btn btn-secondary">✏️ Edit Name</button>
                    </div>

                    <div id="editFridgeForm" class="edit-form hidden">
                    <div class="form-group">
                        <input type="text" id="fridgeName" placeholder="Enter fridge name" maxlength="50" required>
                        <div class="form-actions">
                        <button type="button" class="btn btn-primary" onclick="app.updateFridgeName()">
                            Save
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="app.hideEditForm()">
                            Cancel
                        </button>
                        </div>
                    </div>
                    </div>

                    <div class="fridge-content" id="fridgeContent">
                    <div class="ingredients-section">
                        <div class="ingredients-header">
                        <h3>Ingredients</h3>
                        <span id="ingredientsCount" class="count-badge">${this.ingredientComponent.ingredients.length} ${this.ingredientComponent.ingredients.length === 1 ? 'item' : 'items'}</span>
                        </div>

                    <div id="ingredientsContainer" class="ingredients-container">
                        ${this.ingredientComponent.container}
                    </div>

                    </div>

                    <div class="add-ingredient-section">
                        <h3>Add New Ingredient</h3>
                        <form id="addIngredientForm" class="add-ingredient-form">
                        <input type="text" name="name" placeholder="Ingredient name" required>
                        <input type="number" name="quantity" placeholder="Quantity" step="0.1" min="0" required>
                        <input type="text" name="unit" placeholder="Unit (g, ml, pcs...)" required>
                        <input type="date" name="expiry" placeholder="Expiry date" required>
                        <input type="text" name="category" placeholder="Category (vegetable, dairy...)" required>
                        <button type="submit" class="btn btn-primary">Add Ingredient</button>
                        </form>
                    </div>
                </div>
                    
            </section>
            `;

    }

    async loadFridge() {
        try {
            const fridgeResponse = await apiService.getFridge();
            this.fridge = fridgeResponse.fridge;

            console.log('Fridge loaded:', this.fridge);

            await this.loadIngredients();
        } catch (error) {
            console.error('Error loading fridge:', error);
        }
    }

    async loadIngredients() {
        try {
            const response = await apiService.getIngredients();
            const ingredients = response.ingredients || [];
            console.log('Ingredients loaded:', ingredients);

            this.ingredientComponent.setIngredients(ingredients);
        } catch (error) {
            console.error('Error loading ingredients:', error);
        }
    }

    async addIngredient(name, quantity, unit, expiryDate, category) {
        const success = await this.ingredientComponent.add(name, quantity, unit, expiryDate, category);
        if (success) {
            await this.loadIngredients();
        }
        return success;
    }

    async removeIngredient(ingredientId) {
        const success = await this.ingredientComponent.remove(ingredientId);
        if (success) {
            await this.loadIngredients();
        }
        return success;
    }

    async updateFridgeName() {
        const nameInput = document.getElementById('fridgeName');
        const name = nameInput.value.trim();

        if (!name) {
            alert('Fridge name cannot be empty.');
            nameInput.focus();
            return;
        }

        if (name.length > 50) {
            alert('Fridge name cannot exceed 50 characters.');
            nameInput.focus();
            return;
        }

        try {
            await apiService.updateFridge({ name });
            this.fridge.name = name;
            document.getElementById('fridgeTitle').textContent = name;
            this.hideEditForm();
        } catch (error) {
            console.error('Error updating fridge name:', error);
        }
    }

    showEditForm() {
        const form = document.getElementById('editFridgeForm');
        const input = document.getElementById('fridgeName');

        form.classList.remove('hidden');
        input.value = this.fridge?.name || 'My Fridge';
        input.focus();
    }

    hideEditForm() {
        const form = document.getElementById('editFridgeForm');
        form.classList.add('hidden');
    }

    eventListeners() {
        this.ingredientComponent.eventListeners();

        const editBtn = document.getElementById('editFridgeBtn');
        if (editBtn) {
            editBtn.addEventListener('click', () => {
                this.showEditForm();
            });
        }

        const updateBtn = document.getElementById('updateFridgeBtn');
        if (updateBtn) {
            updateBtn.addEventListener('click', () => {
                this.updateFridgeName();
            });
        }

        const cancelBtn = document.getElementById('cancelEditBtn');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                this.hideEditForm();
            });
        }
    }
}

class FridgeMateApp {
    constructor() {
        this.container = null;
        this.fridgeComponent = null;
    }

    async init() {
        if (!authService.requireAuth()) {
            return;
        }

        this.fridgeComponent = new FridgeComponent();
        await this.fridgeComponent.init();

        this.container = `
            <main class="main-content" id="mainContent">
                <div class="content-wrapper">
                    <span class="welcome-text">Welcome, ${authService.getUsername()}!</span>
                    ${this.fridgeComponent.container}
                </div>
            </main>`;

        document.body.insertAdjacentHTML('beforeend', this.container);

        this.eventListeners();
    }

    async addIngredient(name, quantity, unit, expiryDate, category) {
        if (this.fridgeComponent) {
            await this.fridgeComponent.addIngredient(name, quantity, unit, expiryDate, category);
        }
    }

    async removeIngredient(ingredientId) {
        if (this.fridgeComponent) {
            await this.fridgeComponent.removeIngredient(ingredientId);
        }
    }

    async updateFridgeName() {
        if (this.fridgeComponent) {
            await this.fridgeComponent.updateFridgeName();
        }
    }

    hideEditForm() {
        if (this.fridgeComponent) {
            this.fridgeComponent.hideEditForm();
        }
    }

    eventListeners() {
        this.fridgeComponent.eventListeners();

        document.getElementById('logoutBtn').addEventListener('click', () => {
            authService.logout();
            window.location.href = 'login.html';
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideEditForm();
            }
        });
    }
}

window.app = new FridgeMateApp();

document.addEventListener('DOMContentLoaded', () => {
    window.app.init();
});