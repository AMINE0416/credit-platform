import { useEffect, useState } from "react";
import { getKpis } from "../services/reportingService";
import { exportExcel } from "../services/dossierService";
import type { Kpis } from "../services/reportingService";
import "./AdminHome.css";

const statutLabels: Record<string, string> = {
    NOUVEAU: "Nouveau", EN_COURS: "En cours", PROMESSE: "Promesse",
    INJOIGNABLE: "Injoignable", REGULARISE: "Régularisé", CLOTURE: "Clôturé",
};

const statutColors: Record<string, string> = {
    NOUVEAU: "#3498db", EN_COURS: "#f39c12", PROMESSE: "#9b59b6",
    INJOIGNABLE: "#7f8c8d", REGULARISE: "#27ae60", CLOTURE: "#2c3e50",
};

export default function AdminHome() {
    const [kpis, setKpis] = useState<Kpis | null>(null);
    const [exporting, setExporting] = useState(false);

    useEffect(() => {
        getKpis().then(setKpis).catch(console.error);
    }, []);

    const handleExportExcel = async () => {
        try {
            setExporting(true);
            await exportExcel();
        } catch (error) {
            console.error("Erreur lors de l'export Excel:", error);
            alert("Erreur lors de l'export Excel");
        } finally {
            setExporting(false);
        }
    };

    if (!kpis) return <div>Chargement...</div>;

    return (
        <div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
                <h2 className="page-title" style={{ margin: 0 }}>Tableau de bord</h2>
                <button 
                    className="btn btn-primary" 
                    onClick={handleExportExcel}
                    disabled={exporting}
                >
                    {exporting ? "Export en cours..." : "📊 Exporter en Excel"}
                </button>
            </div>

            <div className="kpi-grid">
                <div className="kpi-card">
                    <span className="kpi-value">{kpis.totalDossiers}</span>
                    <span className="kpi-label">Dossiers total</span>
                </div>
                <div className="kpi-card">
                    <span className="kpi-value">{kpis.dossiersNonAssignes}</span>
                    <span className="kpi-label">Non assignés</span>
                </div>
                <div className="kpi-card">
                    <span className="kpi-value">{kpis.totalAgents}</span>
                    <span className="kpi-label">Agents actifs</span>
                </div>
                <div className="kpi-card">
                    <span className="kpi-value">{kpis.totalAdmins}</span>
                    <span className="kpi-label">Administrateurs</span>
                </div>
            </div>

            <h3 className="section-title">Répartition par statut</h3>
            <div className="statut-grid">
                {Object.entries(kpis.parStatut).map(([statut, count]) => (
                    <div key={statut} className="statut-card" style={{ borderTopColor: statutColors[statut] }}>
                        <span className="statut-count">{count}</span>
                        <span className="statut-label">{statutLabels[statut] || statut}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}