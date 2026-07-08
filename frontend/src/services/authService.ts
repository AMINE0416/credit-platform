import axios from "axios";

const API_URL = "http://localhost:8081/api/auth";

export interface LoginResponse {
    token: string;
    nom: string;
    prenom: string;
    roles: string[];
}

export async function login(email: string, password: string): Promise<LoginResponse> {
    const response = await axios.post<LoginResponse>(`${API_URL}/login`, { email, password });
    return response.data;
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