import React, { useState, useEffect, useMemo } from 'react';
import IngredientList from '../components/IngredientList';
import RecipeModal from '../components/RecipeModal';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#AA336A'];

const MyPieChart = ({ data }) => {
    if (!data || data.length === 0) {
        return <div className="no-data-message">No data available</div>;
    }

    return (
        <div className="chart-wrapper">
            <ResponsiveContainer>
                <PieChart>
                    <Pie
                        data={data}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                    >
                        {data.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={{
                            backgroundColor: 'var(--bg-card)',
                            borderRadius: '12px',
                            border: '1px solid var(--border-color)',
                            boxShadow: 'var(--shadow-lg)',
                            color: 'var(--text-primary)'
                        }}
                        itemStyle={{ color: 'var(--text-primary)' }}
                    />
                    <Legend verticalAlign="bottom" height={36} />
                </PieChart>
            </ResponsiveContainer>
        </div>
    );
};

const MyFridge = ({ ingredients = [], error, onRemove, recipeOfTheDay }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    const stats = useMemo(() => {
        const today = new Date();
        let soonCount = 0;
        let expiredCount = 0;
        const categoryMap = {};

        ingredients.forEach(ingredient => {
            const expiryDate = new Date(ingredient.expiryDate);
            const diffTime = expiryDate.getTime() - today.getTime();
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

            if (diffDays < 0) expiredCount++;
            else if (diffDays <= 3) soonCount++;

            const category = ingredient.category || 'Others';
            categoryMap[category] = (categoryMap[category] || 0) + 1;
        });

        const categoryData = Object.entries(categoryMap).map(([name, value]) => ({ name, value }));

        return { soonCount, expiredCount, categoryData };
    }, [ingredients]);

    return (
        <div className="my-fridge-page">
            <h2 className="page-title">My Fridge</h2>

            <div className="top-container">
                <div className="section-nearing-expiry section-card">
                    <h2>Products Nearing Expiry</h2>
                    {error ? (
                        <p className="error-text">{error}</p>
                    ) : (
                        <IngredientList
                            type="small"
                            sortBy="expiryDate"
                            maxElements={5}
                            ingredients={ingredients}
                            onRemove={onRemove}
                        />
                    )}
                </div>

                <div className="section-fridge-overview section-card">
                    <h2>Fridge Overview</h2>

                    <div className="fridge-overview-details">
                        <div className="stats-grid">
                            <div className="stat-item">
                                <strong>{ingredients.length}</strong>
                                <span>Items</span>
                            </div>
                            <div className="stat-item">
                                <strong>{stats.soonCount}</strong>
                                <span>Expiring Soon</span>
                            </div>
                            <div className="stat-item">
                                <strong>{stats.expiredCount}</strong>
                                <span>Expired</span>
                            </div>
                        </div>

                        <div className="chart-container">
                            <h3>Category Distribution</h3>
                            <MyPieChart data={stats.categoryData} />
                        </div>
                    </div>
                </div>
            </div>

            <div className="section-recipe-suggestions section-card">
                <h2>Recipe of the Day</h2>
                
                {recipeOfTheDay ? (
                    <>
                        <div 
                            className="recipe-day-card" 
                            onClick={() => setIsModalOpen(true)}
                        >
                            <div className="recipe-day-header">
                                <h3>{recipeOfTheDay.name}</h3>
                                <div className="recipe-day-times">
                                    <span className="time-tag">
                                        {recipeOfTheDay.prep_time_min}m prep
                                    </span>
                                    <span className="time-tag">
                                        {recipeOfTheDay.cook_time_min}m cook
                                    </span>
                                </div>
                            </div>
                            
                            <p className="recipe-day-description">{recipeOfTheDay.description}</p>
                            
                            <div className="recipe-day-ingredients-preview">
                                <span className="preview-label">Key Ingredients:</span>
                                <div className="preview-tags">
                                    {recipeOfTheDay.ingredients.slice(0, 4).map((ing, i) => (
                                        <span key={i} className="preview-tag">{ing.item}</span>
                                    ))}
                                    {recipeOfTheDay.ingredients.length > 4 && (
                                        <span className="preview-tag more">+{recipeOfTheDay.ingredients.length - 4} more</span>
                                    )}
                                </div>
                            </div>
                            
                            <div className="recipe-day-cta">
                                Click to view full recipe &rarr;
                            </div>
                        </div>

                    </>
                ) : (
                    <div className="empty-recipe-day">
                        <p>Add ingredients to your fridge to get a personalized recommendation!</p>
                    </div>
                )}

            </div>
            
            <RecipeModal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)}
                recipe={recipeOfTheDay} 
                userIngredients={ingredients}
            />
        </div>
    );
}

export default MyFridge;