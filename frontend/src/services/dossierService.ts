import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api/dossiers`;

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
    nbActions: number;
}

export interface TransitionInfo {
    transitionsAutorisees: string[];
    champsRequis: ChampsRequis;
}

export interface ChampsRequis {
    natureActionObligatoire: boolean;
    motifImpayeVisible: boolean;
    motifImpayeObligatoire: boolean;
    compteRenduObligatoire: boolean;
    codeResultatObligatoire: boolean;
    recidiveVisible: boolean;
    recidiveObligatoire: boolean;
    montantPromesseVisible: boolean;
    montantPromesseObligatoire: boolean;
    datePromesseVisible: boolean;
    datePromesseObligatoire: boolean;
    nouveauTelephoneVisible: boolean;
}

export interface ActionRequest {
    natureAction: string;
    motifImpaye: string;
    compteRendu: string;
    codeResultat: string;
    recidive: "OUI" | "NON";  // Enum, pas boolean
    nbreActionClient: number | null;
    montantPromesse: number | null;
    datePromesse: string | null;
    nouveauTelephone: string | null;
    nouveauStatut: string;
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

export async function getTransitionsAutorisees(id: number, nouveauStatut?: string): Promise<TransitionInfo> {
    const params = nouveauStatut ? { nouveauStatut } : {};
    const res = await axios.get(`${API_URL}/${id}/transitions`, { params });
    return res.data;
}

export async function saveAction(id: number, payload: ActionRequest) {
    return axios.post(`${API_URL}/${id}/actions`, payload);
}

export async function affecterDossiers(dossierIds: number[], agentId: number) {
    return axios.put(`${API_URL}/affectation`, { dossierIds, agentId });
}

/**
 * Exporte tous les dossiers en Excel avec deux feuilles:
 * - État actuel: snapshot de tous les dossiers
 * - Historique statuts: chronologie complète des transitions
 */
export async function exportExcel(): Promise<void> {
    try {
        const response = await axios.get(`${API_URL}/export/excel`, {
            responseType: 'blob',
        });

        // Créer un blob à partir de la réponse
        const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });

        // Extraire le nom du fichier depuis l'en-tête Content-Disposition ou générer un nom par défaut
        const contentDisposition = response.headers['content-disposition'];
        let filename = 'export_dossiers.xlsx';
        
        if (contentDisposition) {
            const filenameMatch = contentDisposition.match(/filename="?(.+)"?/i);
            if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1];
            }
        }

        // Créer un lien de téléchargement et le déclencher
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        
        // Nettoyer
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Erreur lors de l\'export Excel:', error);
        throw error;
    }
}

export async function supprimerDossier(id: number): Promise<void> {
    await axios.delete(`${API_URL}/${id}`);
}