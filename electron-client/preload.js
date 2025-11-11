const { contextBridge } = require('electron');
contextBridge.exposeInMainWorld('electronAPI', {
  fetchItems: async () => {
    const r = await fetch('http://localhost:3000/api/items');
    return r.json();
  }
});
