import axios from "axios";

export interface Kpis {
    totalDossiers: number;
    parStatut: Record<string, number>;
    totalAgents: number;
    totalAdmins: number;
    dossiersNonAssignes: number;
}

export async function getKpis(): Promise<Kpis> {
    const res = await axios.get("http://localhost:8081/api/reporting/kpis");
    return res.data;
}