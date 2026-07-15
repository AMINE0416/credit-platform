import axios from "axios";
import { getToken, logout } from "./authService";

axios.interceptors.request.use((config) => {
    // Ne pas ajouter le token pour les requêtes d'authentification
    if (config.url?.includes('/api/auth/login')) {
        return config;
    }
    
    const token = getToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Ne pas rediriger si on est déjà sur la page de login
            if (!window.location.pathname.includes('/login')) {
                logout();
                window.location.href = "/login";
            }
        }
        return Promise.reject(error);
    }
);