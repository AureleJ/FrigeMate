import React, { useState, useEffect } from 'react';

const Loading = () => {
    const messages = [
        "Checking the fridge...",
        "Slicing tomatoes...",
        "Hiding the snacks...",
        "Cooling the drinks...",
        "Organizing the pantry...",
        "Finding the best recipes...",
        "Warming up the oven...",
        "Tasting the sauce..."
    ];

    const icons = ["🍎", "🥦", "🥕", "🧀", "🍗", "🍕", "🥚", "🥑"];

    const [messageIndex, setMessageIndex] = useState(0);
    const [iconIndex, setIconIndex] = useState(0);

    useEffect(() => {
        const messageInterval = setInterval(() => {
            setMessageIndex(prevIndex => (prevIndex + 1) % messages.length);
        }, 1000);

        const iconInterval = setInterval(() => {
            setIconIndex(prevIndex => (prevIndex + 1) % icons.length);
        }, 800);

        return () => {
            clearInterval(messageInterval);
            clearInterval(iconInterval);
        };
    }, []);

    return (
        <div className="loading-container">
            <div className="bouncing-food">
                {icons[iconIndex]}
            </div>
            <div className="loading-text">
                {messages[messageIndex]}
            </div>
        </div>
    );
};

export default Loading;