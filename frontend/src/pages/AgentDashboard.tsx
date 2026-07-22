import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMesDossiers } from "../services/dossierService";
import { logout } from "../services/authService";
import type { DossierListItem } from "../services/dossierService";
import eqdomLogo from "../assets/eqdom.jpg";
import "./AgentDashboard.css";

const statutColors: Record<string, string> = {
  NOUVEAU: "#FF1744",
  EN_COURS: "#FF6D00",
  PROMESSE: "#D500F9",
  INJOIGNABLE: "#9E9E9E",
  REGULARISE: "#00C853",
  CLOTURE: "#424242",
};

export default function AgentDashboard() {
  const [dossiers, setDossiers] = useState<DossierListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const nom = localStorage.getItem("nom") || "Agent";

  useEffect(() => {
    setLoading(true);
    getMesDossiers()
      .then(setDossiers)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleLogout = () => {
    logout();
    window.location.href = "/login";
  };

  return (
      <div className="agent-page">
        <header className="agent-header">
          <div className="header-left">
            <img src={eqdomLogo} alt="eqdom" className="eqdom-logo" />
            <div className="brand-text">
              <span className="brand-name">eqdom</span>
              <span className="brand-tagline">L'essentiel, c'est vous.</span>
            </div>
          </div>
          <div className="agent-header-right">
            <span className="agent-username">👤 {nom}</span>
            <button className="logout-btn-agent" onClick={handleLogout} title="Se déconnecter">
              🚪 Déconnexion
            </button>
          </div>
        </header>

        <div className="agent-content">
          <h2>Mes dossiers assignés</h2>
          
          {loading ? (
            <div className="loading-container">
              <div className="spinner"></div>
              <p>Chargement des dossiers...</p>
            </div>
          ) : (
            <div className="table-container">
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
          )}
        </div>
      </div>
  );
}
