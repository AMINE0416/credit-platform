import { useEffect, useState } from "react";
import { getKpis } from "../services/reportingService";
import { exportExcel } from "../services/dossierService";
import type { Kpis } from "../services/reportingService";
import "./AdminHome.css";

/* ─── SVG Icons ──────────────────────────────────────────── */
const IconDownload = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
    <polyline points="7 10 12 15 17 10"/>
    <line x1="12" y1="15" x2="12" y2="3"/>
  </svg>
);
const IconLoader = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="spin-icon">
    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
  </svg>
);

const statutLabels: Record<string, string> = {
  NOUVEAU: "Nouveau", EN_COURS: "En cours", PROMESSE: "Promesse",
  INJOIGNABLE: "Injoignable", REGULARISE: "Régularisé", 
  ALERTE: "Alerte", SUIVI_PROMESSE: "Suivi Promesse", 
  SUIVI_INJOIGNABLE: "Suivi Injoignable", CLOTURE: "Clôturé",
};
const statutColors: Record<string, string> = {
  NOUVEAU: "#EF4444", EN_COURS: "#F97316", PROMESSE: "#A855F7",
  INJOIGNABLE: "#9CA3AF", REGULARISE: "#10B981", 
  ALERTE: "#EF4444", SUIVI_PROMESSE: "#8B5CF6", 
  SUIVI_INJOIGNABLE: "#6B7280", CLOTURE: "#374151",
};
const statutBg: Record<string, string> = {
  NOUVEAU: "#FEF2F2", EN_COURS: "#FFF7ED", PROMESSE: "#FAF5FF",
  INJOIGNABLE: "#F9FAFB", REGULARISE: "#F0FDF4", 
  ALERTE: "#FEF2F2", SUIVI_PROMESSE: "#FAF5FF", 
  SUIVI_INJOIGNABLE: "#F9FAFB", CLOTURE: "#F8FAFC",
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

  if (!kpis) return (
    <div className="kpi-loading">
      <div className="spinner-sm"></div>
      <span>Chargement du tableau de bord...</span>
    </div>
  );

  const tauxAssignation = kpis.totalDossiers > 0
    ? ((kpis.totalDossiers - kpis.dossiersNonAssignes) / kpis.totalDossiers * 100).toFixed(1)
    : "0";
  const pourcentageNonAssignes = kpis.totalDossiers > 0
    ? (kpis.dossiersNonAssignes / kpis.totalDossiers * 100).toFixed(1)
    : "0";

  return (
    <div>
      <div className="page-top-row">
        <div>
          <h2 className="page-title">Tableau de bord</h2>
          <p className="page-subtitle">Vue d'ensemble de l'activité</p>
        </div>
        <button
          className="btn-export"
          onClick={handleExportExcel}
          disabled={exporting}
        >
          {exporting ? <IconLoader /> : <IconDownload />}
          {exporting ? "Export en cours..." : "Exporter Excel"}
        </button>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="kpi-card kpi-blue">
          <div className="kpi-icon-wrap kpi-icon-blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div>
            <span className="kpi-value">{kpis.totalDossiers}</span>
            <span className="kpi-label">Dossiers total</span>
          </div>
        </div>

        <div className="kpi-card kpi-orange">
          <div className="kpi-icon-wrap kpi-icon-orange">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <div>
            <span className="kpi-value">{kpis.dossiersNonAssignes}</span>
            <span className="kpi-label">Non assignés <em>{pourcentageNonAssignes}%</em></span>
          </div>
        </div>

        <div className="kpi-card kpi-purple">
          <div className="kpi-icon-wrap kpi-icon-purple">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div>
            <span className="kpi-value">{kpis.totalAgents}</span>
            <span className="kpi-label">Agents actifs</span>
          </div>
        </div>

        <div className="kpi-card kpi-green">
          <div className="kpi-icon-wrap kpi-icon-green">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
            </svg>
          </div>
          <div>
            <span className="kpi-value">{tauxAssignation}%</span>
            <span className="kpi-label">Taux d'assignation</span>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${tauxAssignation}%` }}></div>
            </div>
          </div>
        </div>
      </div>

      {/* Statut breakdown */}
      <h3 className="section-title">Répartition par statut</h3>
      <div className="statut-grid">
        {Object.entries(kpis.parStatut).map(([statut, count]) => {
          const percentage = kpis.totalDossiers > 0
            ? (count / kpis.totalDossiers * 100).toFixed(1)
            : "0";
          return (
            <div key={statut} className="statut-card" style={{ background: statutBg[statut] || "#F8FAFC" }}>
              <div className="statut-dot" style={{ background: statutColors[statut] }}></div>
              <div className="statut-header">
                <span className="statut-count" style={{ color: statutColors[statut] }}>{count}</span>
                <span className="statut-percentage">{percentage}%</span>
              </div>
              <span className="statut-label">{statutLabels[statut] || statut}</span>
              <div className="statut-progress">
                <div
                  className="statut-progress-fill"
                  style={{ width: `${percentage}%`, backgroundColor: statutColors[statut] }}
                ></div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
