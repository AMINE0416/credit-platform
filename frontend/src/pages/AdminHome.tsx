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
    NOUVEAU: "#FF1744", EN_COURS: "#FF6D00", PROMESSE: "#D500F9",
    INJOIGNABLE: "#9E9E9E", REGULARISE: "#00C853", CLOTURE: "#424242",
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

    // Calcul des pourcentages
    const tauxAssignation = kpis.totalDossiers > 0 
        ? ((kpis.totalDossiers - kpis.dossiersNonAssignes) / kpis.totalDossiers * 100).toFixed(1)
        : "0";
    
    const pourcentageNonAssignes = kpis.totalDossiers > 0
        ? (kpis.dossiersNonAssignes / kpis.totalDossiers * 100).toFixed(1)
        : "0";

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
                    <span className="kpi-percentage">{pourcentageNonAssignes}%</span>
                </div>
                <div className="kpi-card">
                    <span className="kpi-value">{kpis.totalAgents}</span>
                    <span className="kpi-label">Agents actifs</span>
                </div>
                <div className="kpi-card kpi-card-highlight">
                    <span className="kpi-value">{tauxAssignation}%</span>
                    <span className="kpi-label">Taux d'assignation</span>
                    <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${tauxAssignation}%` }}></div>
                    </div>
                </div>
            </div>

            <h3 className="section-title">Répartition par statut</h3>
            <div className="statut-grid">
                {Object.entries(kpis.parStatut).map(([statut, count]) => {
                    const percentage = kpis.totalDossiers > 0 
                        ? (count / kpis.totalDossiers * 100).toFixed(1) 
                        : "0";
                    return (
                        <div key={statut} className="statut-card" style={{ borderTopColor: statutColors[statut] }}>
                            <div className="statut-header">
                                <span className="statut-count">{count}</span>
                                <span className="statut-percentage">{percentage}%</span>
                            </div>
                            <span className="statut-label">{statutLabels[statut] || statut}</span>
                            <div className="statut-progress">
                                <div 
                                    className="statut-progress-fill" 
                                    style={{ 
                                        width: `${percentage}%`,
                                        backgroundColor: statutColors[statut]
                                    }}
                                ></div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
