import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api/imports`;
const HISTORIQUE_URL = `${import.meta.env.VITE_API_URL}/api/historique`;
const ARCHIVAGE_URL = `${import.meta.env.VITE_API_URL}/api/archivage`;

export interface ImportRapport {
    jobId: number;
    statut: string;
    nbLignesTotal: number;
    nbInserees: number;
    nbMisesAJour: number;
    nbRejetees: number;
    erreurs: { ligne: number; referenceContrat: string; motif: string }[];
}

export interface ImportAvecDetectionRapport {
    success: boolean;
    message: string;
    moisImporte: string;
    totalDossiers: number;
    nouveauxDossiers: number;
    dossiersRecurrents: number;
    repartitionStatuts: { [key: string]: number };
    repartitionRecurrents: { [key: string]: number };
}

/**
 * Import Excel standard avec le mois d'import
 */
export async function importExcel(file: File, moisImport?: string): Promise<ImportRapport> {
    const formData = new FormData();
    formData.append("file", file);
    if (moisImport) {
        formData.append("moisImport", moisImport);
    }
    const res = await axios.post(`${API_URL}/excel`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
}

/**
 * Import Excel avec détection automatique des dossiers récurrents du mois précédent
 */
export async function importExcelAvecDetection(file: File, moisImport: string): Promise<ImportAvecDetectionRapport> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("moisImport", moisImport);
    const res = await axios.post(`${API_URL}/avec-detection`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
}

export async function resetDatabase(): Promise<any> {
    const res = await axios.delete(`${API_URL}/reset-database`);
    return res.data;
}

// ============ Fonctions Historique ============

export interface PeriodeArchivee {
    mois: number;
    annee: number;
    nbDossiers: number;
}

export interface DossierHistorique {
    id: number;
    mois: number;
    annee: number;
    dateArchivage: string;
    dossierIdOrigine: number;
    statutDossier: string;
    agentAssigneEmail: string;
    agentAssigneNom: string;
    cin: string;
    nom: string;
    prenom: string;
    referenceContrat: string;
    ville: string;
    score: number;
    nbActionsTotal: number;
    dateDerniereAction: string;
}

/**
 * Archiver tous les dossiers actuels pour un mois donné (ancien système)
 * À appeler AVANT chaque import de nouvelles données
 */
export async function archiverDossiers(mois: number, annee: number): Promise<any> {
    const res = await axios.post(`${HISTORIQUE_URL}/archiver`, null, {
        params: { mois, annee }
    });
    return res.data;
}

/**
 * Récupérer la liste des périodes archivées
 */
export async function getPeriodesArchivees(): Promise<PeriodeArchivee[]> {
    const res = await axios.get(`${HISTORIQUE_URL}/periodes`);
    return res.data;
}

/**
 * Récupérer tous les dossiers archivés pour une période donnée
 */
export async function getDossiersArchives(mois: number, annee: number): Promise<DossierHistorique[]> {
    const res = await axios.get(`${HISTORIQUE_URL}/dossiers`, {
        params: { mois, annee }
    });
    return res.data;
}

/**
 * Récupérer l'historique complet d'un dossier à travers les différents mois
 */
export async function getHistoriqueDossier(dossierIdOrigine: number): Promise<DossierHistorique[]> {
    const res = await axios.get(`${HISTORIQUE_URL}/dossier/${dossierIdOrigine}`);
    return res.data;
}

// ============ Fonctions Archivage Mensuel (nouveau système unifié) ============

/**
 * Télécharger le fichier Excel archivé pour un mois
 */
export async function telechargerCsvArchive(mois: string): Promise<void> {
    try {
        const response = await axios.get(`${ARCHIVAGE_URL}/telecharger-csv/${mois}`, {
            responseType: 'blob', // Important pour télécharger des fichiers binaires
        });
        
        // Créer un lien temporaire pour télécharger le fichier
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `export_archive_${mois}.xlsx`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Erreur lors du téléchargement du CSV archivé:', error);
        throw error;
    }
}

/**
 * Récupérer la liste des exports disponibles
 */
export async function getExportsDisponibles(): Promise<any[]> {
    const res = await axios.get(`${ARCHIVAGE_URL}/exports`);
    return res.data;
}