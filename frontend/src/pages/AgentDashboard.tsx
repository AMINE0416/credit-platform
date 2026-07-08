import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMesDossiers } from "../services/dossierService";
import type { DossierListItem } from "../services/dossierService";
import "./AgentDashboard.css";

const statutColors: Record<string, string> = {
  NOUVEAU: "#3498db",
  EN_COURS: "#f39c12",
  PROMESSE: "#9b59b6",
  INJOIGNABLE: "#7f8c8d",
  REGULARISE: "#27ae60",
  CLOTURE: "#2c3e50",
};

export default function AgentDashboard() {
  const [dossiers, setDossiers] = useState<DossierListItem[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    getMesDossiers().then(setDossiers).catch(console.error);
  }, []);

  return (
      <div className="agent-page">
        <header className="agent-header">
          <span>Centre_Aouad</span>
          <div className="agent-header-logo">
            <span className="logo-circle">C</span>
            <div className="logo-text"><strong>eqdom</strong><small>L'essentiel, c'est vous.</small></div>
          </div>
        </header>

        <div className="agent-content">
          <h2>Mes dossiers assignés</h2>
          <table className="dossier-table">
            <thead>
            <tr>
              <th>CIN</th>
              <th>Nom complet</th>
              <th>Référence contrat</th>
              <th>Ville</th>
              <th>Statut</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            {dossiers.map((d) => (
                <tr key={d.id}>
                  <td>{d.cin}</td>
                  <td>{d.nomComplet}</td>
                  <td>{d.referenceContrat}</td>
                  <td>{d.ville}</td>
                  <td>
                  <span className="badge" style={{ background: statutColors[d.statut] }}>
                    {d.statut}
                  </span>
                  </td>
                  <td>
                    <button onClick={() => navigate(`/agent/dossiers/${d.id}`)}>
                      Ouvrir
                    </button>
                  </td>
                </tr>
            ))}
            {dossiers.length === 0 && (
                <tr><td colSpan={6} className="empty">Aucun dossier assigné pour le moment</td></tr>
            )}
            </tbody>
          </table>
        </div>
      </div>
  );
}