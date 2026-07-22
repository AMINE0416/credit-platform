import axios from "axios";

export interface Kpis {
    totalDossiers: number;
    parStatut: Record<string, number>;
    totalAgents: number;
    totalAdmins: number;
    dossiersNonAssignes: number;
}

export async function getKpis(): Promise<Kpis> {
    const res = await axios.get(`${import.meta.env.VITE_API_URL}/api/reporting/kpis`);
    return res.data;
}