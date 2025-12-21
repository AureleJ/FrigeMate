export const config = {
    API_URL: import.meta.env.VITE_API_URL || 'http://localhost:3000',
    
    API_KEY: import.meta.env.VITE_API_KEY || '',

    STORAGE_KEYS: {
        USER_ID: 'fridgemate_user_id',
        USERNAME: 'fridgemate_username'
    }
};
