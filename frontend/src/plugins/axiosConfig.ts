import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || window.env?.VITE_API_BASE_URL,  // Fallback to window.env in production if available
  headers: {
    'Content-Type': 'application/json',
  },
});

export default apiClient;
