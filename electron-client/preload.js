const { contextBridge } = require('electron');

const baseURL = 'http://localhost:3000';

async function request(endpoint, options = {}) {
  const url = `${baseURL}${endpoint}`;

  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json'
    }
  };

  const requestOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };

  console.log(`API Request: ${requestOptions.method || 'GET'} ${url}`);

  try {
    const response = await fetch(url, requestOptions);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error(`API Error for ${endpoint}:`, error.message);
    throw error;
  }
}


contextBridge.exposeInMainWorld('API', {
  getUser: async (userId) => {
    return await request(`/users/${userId}`);
  }
});
