import axios from "axios";

const API_URL = "http://localhost:8081/api/dossiers";

export interface DossierListItem {
    id: number;
    cin: string;
    nomComplet: string;
    referenceContrat: string;
    statut: string;
    ville: string;
    agentAssigne: string;
}

export interface DossierDetail {
    dossierId: number;
    statut: string;
    client: any;
    credit: any;
}

export async function getDossiers(filters?: { statut?: string; ville?: string; agentId?: number }): Promise<DossierListItem[]> {
    const res = await axios.get(API_URL, { params: filters });
    return res.data;
}

export async function getMesDossiers(): Promise<DossierListItem[]> {
    const res = await axios.get(`${API_URL}/me`);
    return res.data;
}

export async function getDossier(id: number): Promise<DossierDetail> {
    const res = await axios.get(`${API_URL}/${id}`);
    return res.data;
}

export async function saveAction(id: number, payload: any) {
    return axios.post(`${API_URL}/${id}/actions`, payload);
}

export async function affecterDossiers(dossierIds: number[], agentId: number) {
    return axios.put(`${API_URL}/affectation`, { dossierIds, agentId });
}