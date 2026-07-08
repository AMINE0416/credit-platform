import axios from "axios";
import { getToken, logout } from "./authService";

axios.interceptors.request.use((config) => {
    const token = getToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            logout();
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);