import axios from "axios";

const API_URL = "http://localhost:8081/api/imports";

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