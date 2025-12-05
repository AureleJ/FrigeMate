import React from 'react';

const IngredientItem = ({ type, ingredient, onRemove }) => {
    const today = new Date();
    const expiryDate = new Date(ingredient.expiryDate);

    const diffTime = expiryDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    const isExpired = diffDays < 0;
    const isExpiringSoon = diffDays >= 0 && diffDays <= 3;

    let statusClass = '';
    if (isExpired) {
        statusClass = 'expired';
    } else if (isExpiringSoon) {
        statusClass = 'expiring-soon';
    }

    const imageUrl = `https://www.themealdb.com/images/ingredients/${ingredient.name}-small.png`;

    const urgencyBadge = isExpired ?
        'Expired' :
        (isExpiringSoon ? `Expires in ${diffDays} day${diffDays > 1 ? 's' : ''}` : null);

    if (type === 'small') {
        return (
            <div className={`ingredient-item small`}>
                <div className="ingredient-image-wrapper small">
                    <img src={imageUrl} alt={ingredient.name} className="ingredient-image" />
                </div>

                <div className="ingredient-info-small">
                    <h4 className="ingredient-name">{ingredient.name}</h4>

                    <div className="ingredient-details-small">
                        <span className="ingredient-quantity-text">
                            {ingredient.quantity} {ingredient.unit}
                        </span>
                        <span className="ingredient-category-text">
                            {ingredient.category || 'Other'}
                        </span>
                    </div>
                </div>

                {urgencyBadge && (
                    <div className="urgency-indicator">
                        <span className="urgency-dot" style={{ backgroundColor: isExpired ? '#ea5640' : '#eda82fff' }}></span>
                        <span className="urgency-text-small">{isExpired ? 'Expired' : `${diffDays} day${diffDays > 1 ? 's' : ''}`}</span>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className={`ingredient-item ${statusClass}`}>
            <button className="btn-remove" onClick={() => onRemove(ingredient.id)} title="Remove">
                ×
            </button>

            <div className="ingredient-header">
                <div className="ingredient-image-wrapper">
                    <img src={imageUrl} alt={ingredient.name} className="ingredient-image" />
                </div>
                <div className="ingredient-right-header">
                    <h4 className="ingredient-name">{ingredient.name}</h4>
                    {urgencyBadge && (
                        <span className="urgency-badge" style={{ backgroundColor: isExpired ? '#ea5640' : '#eda82fff' }}>
                            {urgencyBadge}
                        </span>
                    )}
                </div>
            </div>

            <div className="ingredient-info">
                <div className="ingredient-details">
                    <span className="ingredient-quantity">{ingredient.quantity} {ingredient.unit}</span>
                    <span className="ingredient-category">{ingredient.category || 'Other'}</span>
                </div>

                <div className="ingredient-expiry-row">
                    <span className="label">Expires:</span>
                    <span className="value">{new Date(ingredient.expiryDate).toLocaleDateString()}</span>
                </div>
            </div>
        </div>
    );
};

export default IngredientItem;