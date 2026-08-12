import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getDossiers, affecterDossiers, supprimerDossier } from "../services/dossierService";
import type { DossierListItem } from "../services/dossierService";
import { getUsers } from "../services/userService";
import type { UserDTO } from "../services/userService";
import "./AdminDossiers.css";

/* ─── SVG Icons ──────────────────────────────────────────── */
const IconTrash = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
    <path d="M10 11v6"/><path d="M14 11v6"/>
    <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
  </svg>
);
const IconAssign = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
    <circle cx="9" cy="7" r="4"/>
    <line x1="19" y1="8" x2="19" y2="14"/>
    <line x1="22" y1="11" x2="16" y2="11"/>
  </svg>
);
const IconSearch = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
);
const IconXSmall = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
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

export default function AdminDossiers() {
  const [dossiers, setDossiers] = useState<DossierListItem[]>([]);
  const [allVilles, setAllVilles] = useState<string[]>([]);
  const [agents, setAgents] = useState<UserDTO[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [filterStatut, setFilterStatut] = useState("");
  const [filterVille, setFilterVille] = useState("");
  const [filterAgent, setFilterAgent] = useState<number | "">("");  // ← nouveau filtre agent
  const [cinSearch, setCinSearch] = useState("");           // ← nouveau
  const [agentCible, setAgentCible] = useState<number | "">("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // Filtre CIN côté client (instantané, pas besoin de recharger)
  const filteredDossiers = cinSearch.trim()
    ? dossiers.filter(d => d.cin.toLowerCase().includes(cinSearch.trim().toLowerCase()))
    : dossiers;

  const loadDossiers = () => {
    setLoading(true);
    getDossiers({ 
      statut: filterStatut || undefined, 
      ville: filterVille || undefined,
      agentId: filterAgent ? Number(filterAgent) : undefined
    })
      .then(data => {
        setDossiers(data);
        setSelected([]); // réinitialiser la sélection quand les données changent
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  // Charger TOUTES les villes une seule fois au montage (sans filtre)
  useEffect(() => {
    getDossiers({})
      .then(data => {
        const villes = [...new Set(data.map(d => d.ville).filter(Boolean))].sort();
        setAllVilles(villes);
      })
      .catch(console.error);
  }, []);

  useEffect(() => { loadDossiers(); }, [filterStatut, filterVille, filterAgent]);
  useEffect(() => {
    getUsers().then(users => setAgents(users.filter(u => u.roles.includes("AGENT"))));
  }, []);

  const toggleSelect = (id: number) => {
    setSelected(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const toggleAll = () => {
    setSelected(selected.length === dossiers.length ? [] : dossiers.map(d => d.id));
  };

  const handleAffecter = async () => {
    if (!agentCible || selected.length === 0) {
      alert("Sélectionnez au moins un dossier et un agent");
      return;
    }
    await affecterDossiers(selected, Number(agentCible));
    alert(`${selected.length} dossier(s) affecté(s)`);
    setSelected([]);
    loadDossiers();
  };

  // allVilles est déjà chargé séparément — pas besoin de dériver depuis dossiers filtrés

  const handleSupprimerSelected = async () => {
    if (selected.length === 0) {
      alert("Sélectionnez au moins un dossier à supprimer");
      return;
    }
    const confirmDelete = window.confirm(
      `Êtes-vous sûr de vouloir supprimer ${selected.length} dossier(s) ? Cette action est irréversible.`
    );
    if (!confirmDelete) return;

    try {
      let supprimis = 0, erreurs = 0;
      for (const id of selected) {
        try { await supprimerDossier(id); supprimis++; }
        catch (error) { console.error(`Erreur suppression dossier ${id}:`, error); erreurs++; }
      }
      alert(`${supprimis} dossier(s) supprimé(s)${erreurs > 0 ? ` - ${erreurs} erreur(s)` : ""}`);
      setSelected([]);
      loadDossiers();
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
      alert("Erreur lors de la suppression des dossiers");
    }
  };

  return (
    <div>
      <div className="page-top-row">
        <div>
          <h2 className="page-title">Gestion des dossiers</h2>
          <p className="page-subtitle">{dossiers.length} dossier{dossiers.length !== 1 ? "s" : ""} au total</p>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-bar">
        <div className="filters-left">
            {/* Recherche CIN */}
            <div className="ad-cin-wrap">
              <span className="ad-cin-icon"><IconSearch /></span>
              <input
                className="ad-cin-input"
                type="text"
                placeholder="Rechercher par CIN…"
                value={cinSearch}
                onChange={e => setCinSearch(e.target.value)}
                autoComplete="off"
              />
              {cinSearch && (
                <button className="ad-cin-clear" onClick={() => setCinSearch("")} title="Effacer">
                  <IconXSmall />
                </button>
              )}
            </div>

          <select value={filterStatut} onChange={e => setFilterStatut(e.target.value)}>
            <option value="">Tous les statuts</option>
            <option value="EN_COURS">En cours</option>
            <option value="PROMESSE">Promesse</option>
            <option value="INJOIGNABLE">Injoignable</option>
            <option value="REGULARISE">Régularisé</option>
            <option value="ALERTE">Alerte</option>
            <option value="SUIVI_PROMESSE">Suivi promesse</option>
            <option value="SUIVI_INJOIGNABLE">Suivi injoignable</option>
            <option value="CLOTURE">Clôturé</option>
          </select>
          <select value={filterVille} onChange={e => setFilterVille(e.target.value)}>
            <option value="">Toutes les villes</option>
            {allVilles.map(v => <option key={v} value={v}>{v}</option>)}
          </select>
          <select value={filterAgent} onChange={e => setFilterAgent(e.target.value as any)}>
            <option value="">Tous les agents</option>
            <option value="0">Non assignés</option>
            {agents.map(a => <option key={a.id} value={a.id}>{a.prenom} {a.nom}</option>)}
          </select>
        </div>

        <div className="affectation-bar">
          <select value={agentCible} onChange={e => setAgentCible(e.target.value as any)}>
            <option value="">— Affecter à un agent —</option>
            {agents.map(a => <option key={a.id} value={a.id}>{a.prenom} {a.nom}</option>)}
          </select>
          <button className="btn-assign" onClick={handleAffecter}>
            <IconAssign />
            Affecter ({selected.length})
          </button>
          <button className="btn-danger" onClick={handleSupprimerSelected}>
            <IconTrash />
            Supprimer ({selected.length})
          </button>
        </div>
      </div>

      {/* Compteur */}
      {!loading && (
        <p className="ad-count">
          {filteredDossiers.length} dossier{filteredDossiers.length !== 1 ? "s" : ""}
          {cinSearch && <span className="ad-count-highlight"> · CIN «{cinSearch}»</span>}
          {dossiers.length !== filteredDossiers.length && (
            <span className="ad-count-total"> (sur {dossiers.length} chargés)</span>
          )}
        </p>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Chargement des dossiers...</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    checked={selected.length === dossiers.length && dossiers.length > 0}
                    onChange={toggleAll}
                    title="Tout sélectionner"
                  />
                </th>
                <th>Score</th>
                <th>CIN</th>
                <th>Nom complet</th>
                <th>Réf. contrat</th>
                <th>Ville</th>
                <th>Agent assigné</th>
                <th>Statut</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredDossiers.map(d => (
                <tr key={d.id} className={selected.includes(d.id) ? "row-selected" : ""}>
                  <td><input type="checkbox" checked={selected.includes(d.id)} onChange={() => toggleSelect(d.id)} /></td>
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
                    {d.agentAssigne
                      ? <span className="agent-name">{d.agentAssigne}</span>
                      : <span className="unassigned">Non assigné</span>}
                  </td>
                  <td>
                    <span className="badge" style={{ background: statutColors[d.statut] }}>
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
                <tr><td colSpan={9} className="empty">
                  {cinSearch
                    ? `Aucun dossier trouvé pour le CIN « ${cinSearch} »`
                    : "Aucun dossier trouvé"}
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
