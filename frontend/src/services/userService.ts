import axios from "axios";

const API_URL = "http://localhost:8081/api/users";

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

export async function updateUser(id: number, payload: any) {
    return axios.put(`${API_URL}/${id}`, payload);
}

export async function toggleActivation(id: number) {
    return axios.put(`${API_URL}/${id}/activation`);
}

export async function resetPassword(id: number, nouveauMotDePasse: string) {
    return axios.put(`${API_URL}/${id}/reset-password`, { nouveauMotDePasse });
}