import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMesDossiers } from "../services/dossierService";
import { logout } from "../services/authService";
import type { DossierListItem } from "../services/dossierService";
import eqdomLogo from "../assets/eqdom.jpg";
import "./AgentDashboard.css";

/* ─── SVG Icons ─────────────────────────────────────────────────── */
const IconUser = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
  </svg>
);
const IconLogout = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
  </svg>
);
const IconSearch = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
);
const IconX = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);
const IconFolder = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
  </svg>
);

const statutColors: Record<string, string> = {
  EN_COURS: "#F97316",           // Orange - En cours
  PROMESSE: "#A855F7",            // Violet - Promesse
  INJOIGNABLE: "#9CA3AF",         // Gris - Injoignable
  REGULARISE: "#10B981",          // Vert - Régularisé
  ALERTE: "#EF4444",              // Rouge vif - Alerte (récurrent non résolu)
  SUIVI_PROMESSE: "#8B5CF6",      // Violet foncé - Suivi promesse
  SUIVI_INJOIGNABLE: "#6B7280",   // Gris foncé - Suivi injoignable
  CLOTURE: "#374151",             // Gris très foncé - Clôturé
};

const statutLabels: Record<string, string> = {
  EN_COURS: "En cours",
  PROMESSE: "Promesse",
  INJOIGNABLE: "Injoignable",
  REGULARISE: "Régularisé",
  ALERTE: "Alerte",
  SUIVI_PROMESSE: "Suivi promesse",
  SUIVI_INJOIGNABLE: "Suivi injoignable",
  CLOTURE: "Clôturé",
};

const STATUTS = [
  { key: "TOUS",               label: "Tous" },
  { key: "EN_COURS",           label: "En cours" },
  { key: "PROMESSE",           label: "Promesse" },
  { key: "INJOIGNABLE",        label: "Injoignable" },
  { key: "REGULARISE",         label: "Régularisé" },
  { key: "ALERTE",             label: "Alerte" },
  { key: "SUIVI_PROMESSE",     label: "Suivi promesse" },
  { key: "SUIVI_INJOIGNABLE",  label: "Suivi injoignable" },
  { key: "CLOTURE",            label: "Clôturé" },
] as const;

export default function AgentDashboard() {
  const [dossiers, setDossiers] = useState<DossierListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [cinSearch, setCinSearch] = useState("");
  const [statutFilter, setStatutFilter] = useState("TOUS");
  const navigate = useNavigate();

  const filteredDossiers = dossiers.filter((d) => {
    const matchCin    = d.cin.toLowerCase().includes(cinSearch.trim().toLowerCase());
    const matchStatut = statutFilter === "TOUS" || d.statut === statutFilter;
    return matchCin && matchStatut;
  });
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
          <div className="user-chip">
            <IconUser />
            <span>{nom}</span>
          </div>
          <button className="logout-btn-agent" onClick={handleLogout} title="Se déconnecter">
            <IconLogout />
            Déconnexion
          </button>
        </div>
      </header>

      <div className="agent-content">
        <div className="dashboard-header-row">
          <div className="page-title-block">
            <IconFolder />
            <h2>Mes dossiers assignés</h2>
          </div>
          <div className="cin-search-wrapper">
            <span className="cin-search-icon"><IconSearch /></span>
            <input
              id="cin-search-input"
              type="text"
              className="cin-search-input"
              placeholder="Rechercher par CIN..."
              value={cinSearch}
              onChange={(e) => setCinSearch(e.target.value)}
              autoComplete="off"
            />
            {cinSearch && (
              <button
                className="cin-search-clear"
                onClick={() => setCinSearch("")}
                title="Effacer"
              >
                <IconX />
              </button>
            )}
          </div>
        </div>

        {/* ── Statut pills ── */}
        <div className="statut-pills">
          {STATUTS.map(s => {
            const count = s.key === "TOUS"
              ? dossiers.length
              : dossiers.filter(d => d.statut === s.key).length;
            const isActive = statutFilter === s.key;
            return (
              <button
                key={s.key}
                className={`statut-pill${isActive ? " statut-pill-active" : ""}`}
                style={isActive && s.key !== "TOUS"
                  ? { background: statutColors[s.key], color: "white", borderColor: statutColors[s.key] }
                  : {}}
                onClick={() => setStatutFilter(s.key)}
              >
                {s.label}
                <span className={`pill-count${isActive ? " pill-count-active" : ""}`}>{count}</span>
              </button>
            );
          })}
        </div>

        {!loading && (
          <p className="results-count">
            {filteredDossiers.length} dossier{filteredDossiers.length !== 1 ? "s" : ""}
            {statutFilter !== "TOUS" && ` · ${STATUTS.find(s => s.key === statutFilter)?.label}`}
            {cinSearch && ` · CIN « ${cinSearch} »`}
          </p>
        )}

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
                  <th>Score</th>
                  <th>CIN</th>
                  <th>Nom complet</th>
                  <th>Réf. contrat</th>
                  <th>Ville</th>
                  <th>Statut</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredDossiers.map((d) => (
                  <tr key={d.id}>
                    <td>
                      {d.score != null ? (
                        <span className="score-badge">{d.score.toFixed(0)}%</span>
                      ) : (
                        <span className="score-badge-empty">—</span>
                      )}
                    </td>
                    <td><span className="mono">{d.cin}</span></td>
                    <td><strong>{d.nomComplet}</strong></td>
                    <td><span className="ref-tag">{d.referenceContrat}</span></td>
                    <td>{d.ville}</td>
                    <td>
                      <span className="badge" style={{ background: statutColors[d.statut] || "#6B7280" }}>
                        {statutLabels[d.statut] || d.statut}
                      </span>
                    </td>
                    <td>
                      <button className="btn-open" onClick={() => navigate(`/agent/dossiers/${d.id}`)}>
                        Ouvrir
                      </button>
                    </td>
                  </tr>
                ))}
                {filteredDossiers.length === 0 && (
                  <tr>
                    <td colSpan={7} className="empty">
                      {cinSearch
                        ? `Aucun dossier trouvé pour le CIN « ${cinSearch} »`
                        : "Aucun dossier assigné pour le moment"}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
