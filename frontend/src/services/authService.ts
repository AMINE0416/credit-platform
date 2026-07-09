import axios from "axios";

const API_URL = "http://localhost:8081/api/auth";

export interface LoginResponse {
    token: string;
    nom: string;
    prenom: string;
    roles: string[];
}

export async function login(email: string, password: string): Promise<LoginResponse> {
    try {
        const response = await axios.post<LoginResponse>(`${API_URL}/login`, { email, password });
        return response.data;
    } catch (error: any) {
        if (error.response) {
            // Erreur retournée par le serveur
            throw new Error(error.response.data || "Email ou mot de passe incorrect");
        } else if (error.request) {
            // Pas de réponse du serveur
            throw new Error("Impossible de contacter le serveur. Vérifiez que le backend est démarré.");
        } else {
            // Autre erreur
            throw new Error("Une erreur est survenue lors de la connexion");
        }
    }
}

export function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("roles");
    localStorage.removeItem("nom");
}

export function getToken() {
    return localStorage.getItem("token");
}

export function getRoles(): string[] {
    const roles = localStorage.getItem("roles");
    return roles ? JSON.parse(roles) : [];
}