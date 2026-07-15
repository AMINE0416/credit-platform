import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getDossiers, affecterDossiers } from "../services/dossierService";
import type { DossierListItem } from "../services/dossierService";
import { getUsers } from "../services/userService";
import type { UserDTO } from "../services/userService";
import "./AdminDossiers.css";

const statutColors: Record<string, string> = {
    NOUVEAU: "#FF1744", EN_COURS: "#FF6D00", PROMESSE: "#D500F9",
    INJOIGNABLE: "#9E9E9E", REGULARISE: "#00C853", CLOTURE: "#424242",
};

export default function AdminDossiers() {
    const [dossiers, setDossiers] = useState<DossierListItem[]>([]);
    const [agents, setAgents] = useState<UserDTO[]>([]);
    const [selected, setSelected] = useState<number[]>([]);
    const [filterStatut, setFilterStatut] = useState("");
    const [filterVille, setFilterVille] = useState("");
    const [agentCible, setAgentCible] = useState<number | "">("");
    const navigate = useNavigate();

    const loadDossiers = () => {
        getDossiers({
            statut: filterStatut || undefined,
            ville: filterVille || undefined,
        }).then(setDossiers).catch(console.error);
    };

    useEffect(() => { loadDossiers(); }, [filterStatut, filterVille]);
    useEffect(() => {
        getUsers().then(users => setAgents(users.filter(u => u.roles.includes("AGENT"))));
    }, []);

    const toggleSelect = (id: number) => {
        setSelected(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
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

    const villes = [...new Set(dossiers.map(d => d.ville))];

    return (
        <div>
            <h2 className="page-title">Gestion des dossiers</h2>

            <div className="filters-bar">
                <select value={filterStatut} onChange={e => setFilterStatut(e.target.value)}>
                    <option value="">Tous les statuts</option>
                    <option value="NOUVEAU">Nouveau</option>
                    <option value="EN_COURS">En cours</option>
                    <option value="PROMESSE">Promesse</option>
                    <option value="INJOIGNABLE">Injoignable</option>
                    <option value="REGULARISE">Régularisé</option>
                    <option value="CLOTURE">Clôturé</option>
                </select>
                <select value={filterVille} onChange={e => setFilterVille(e.target.value)}>
                    <option value="">Toutes les villes</option>
                    {villes.map(v => <option key={v} value={v}>{v}</option>)}
                </select>

                <div className="affectation-bar">
                    <select value={agentCible} onChange={e => setAgentCible(e.target.value as any)}>
                        <option value="">-- Choisir un agent --</option>
                        {agents.map(a => <option key={a.id} value={a.id}>{a.prenom} {a.nom}</option>)}
                    </select>
                    <button className="btn-primary" onClick={handleAffecter}>
                        Affecter ({selected.length})
                    </button>
                </div>
            </div>

            <table className="admin-table">
                <thead>
                <tr>
                    <th></th>
                    <th>CIN</th>
                    <th>Nom complet</th>
                    <th>Référence contrat</th>
                    <th>Ville</th>
                    <th>Agent assigné</th>
                    <th>Statut</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {dossiers.map(d => (
                    <tr key={d.id}>
                        <td><input type="checkbox" checked={selected.includes(d.id)} onChange={() => toggleSelect(d.id)} /></td>
                        <td>{d.cin}</td>
                        <td>{d.nomComplet}</td>
                        <td>{d.referenceContrat}</td>
                        <td>{d.ville}</td>
                        <td>{d.agentAssigne}</td>
                        <td>
                            <span className="badge" style={{ background: statutColors[d.statut] }}>{d.statut}</span>
                        </td>
                        <td><button className="btn-open" onClick={() => navigate(`/agent/dossiers/${d.id}`)}>Ouvrir</button></td>
                    </tr>
                ))}
                {dossiers.length === 0 && (
                    <tr><td colSpan={8} className="empty">Aucun dossier trouvé</td></tr>
                )}
                </tbody>
            </table>
        </div>
    );
}
