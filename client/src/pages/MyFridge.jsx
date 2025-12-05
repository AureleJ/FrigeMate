import React, { useState, useEffect, useMemo } from 'react';
import IngredientList from '../components/IngredientList';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#AA336A'];

const MyPieChart = ({ data }) => {
    if (!data || data.length === 0) {
        return <div style={{ textAlign: 'center', padding: '20px', color: '#888' }}>No data available</div>;
    }

    return (
        <div style={{ width: '100%', height: 300 }}>
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
                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                            borderRadius: '12px',
                            border: 'none',
                            boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                        }}
                    />
                    <Legend verticalAlign="bottom" height={36} />
                </PieChart>
            </ResponsiveContainer>
        </div>
    );
};

const MyFridge = ({ ingredients = [], error, onRemove }) => {
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
            <h1>My Fridge</h1>

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
                <p className="placeholder-text">Based on your fridge content...</p>
            </div>
        </div>
    );
}

export default MyFridge;