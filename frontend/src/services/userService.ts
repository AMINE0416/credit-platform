import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api/users`;

export interface UserDTO {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    actif: boolean;
    roles: string[];
    nbDossiersAssignes: number;
}

export async function getUsers(): Promise<UserDTO[]> {
    const res = await axios.get(API_URL);
    return res.data;
}

export async function createUser(payload: any) {
    return axios.post(API_URL, payload);
}

/**
 * Met à jour le profil utilisateur (nom, prénom, email).
 * N'inclut PAS le champ role pour éviter le mass assignment.
 */
export async function updateUser(id: number, payload: { nom: string; prenom: string; email: string }) {
    return axios.put(`${API_URL}/${id}`, payload);
}

/**
 * Met à jour le rôle d'un utilisateur.
 * Réservé aux administrateurs.
 */
export async function updateUserRole(id: number, role: string) {
    return axios.put(`${API_URL}/${id}/role`, { role });
}

export async function toggleActivation(id: number) {
    return axios.put(`${API_URL}/${id}/activation`);
}

export async function resetPassword(id: number, nouveauMotDePasse: string) {
    return axios.put(`${API_URL}/${id}/reset-password`, { nouveauMotDePasse });
}