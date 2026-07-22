import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api/imports`;

export interface ImportRapport {
    jobId: number;
    statut: string;
    nbLignesTotal: number;
    nbInserees: number;
    nbMisesAJour: number;
    nbRejetees: number;
    erreurs: { ligne: number; referenceContrat: string; motif: string }[];
}

export async function importExcel(file: File): Promise<ImportRapport> {
    const formData = new FormData();
    formData.append("file", file);
    const res = await axios.post(`${API_URL}/excel`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
}

export async function resetDatabase(): Promise<any> {
    const res = await axios.delete(`${API_URL}/reset-database`);
    return res.data;
}