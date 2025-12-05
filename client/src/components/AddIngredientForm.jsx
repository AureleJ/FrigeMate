import React, { useState } from 'react';

const AddIngredientForm = ({ onAdd, onClosePopup }) => {
    const [formData, setFormData] = useState({
        name: '',
        quantity: '',
        unit: '',
        expiryDate: '',
        category: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        console.log('Submitting form with data:', formData);
        e.preventDefault();
        if (!formData.name) return;

        onAdd({
            ...formData,
            quantity: parseFloat(formData.quantity) || 0
        });

        setFormData({
            name: '',
            quantity: '',
            unit: '',
            expiryDate: '',
            category: ''
        });
    };

    return (
        <div className="popup" >
            <div className="add-ingredient-section">
                <button className="close-btn" onClick={onClosePopup}>×</button>
                
                <h3>Add New Ingredient</h3>
                <form onSubmit={handleSubmit} className="add-ingredient-form">
                    <input
                        type="text"
                        name="name"
                        placeholder="Name (e.g. Milk)"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="number"
                        name="quantity"
                        placeholder="Qty"
                        value={formData.quantity}
                        onChange={handleChange}
                        step="0.1"
                    />
                    <input
                        type="text"
                        name="unit"
                        placeholder="Unit (e.g. L)"
                        value={formData.unit}
                        onChange={handleChange}
                    />
                    <input
                        type="date"
                        name="expiryDate"
                        value={formData.expiryDate}
                        onChange={handleChange}
                    />
                    <input
                        type="text"
                        name="category"
                        placeholder="Category"
                        value={formData.category}
                        onChange={handleChange}
                    />
                    <button type="submit" className="btn btn-primary">Add</button>
                </form>
            </div>
        </div>
    );
};

export default AddIngredientForm;
