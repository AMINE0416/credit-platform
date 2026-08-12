import { useEffect, useState } from "react";
import { getUsers, createUser, toggleActivation, resetPassword } from "../services/userService";
import type { UserDTO } from "../services/userService";
import "./AdminUsers.css";

/* ─── SVG Icons ──────────────────────────────────────────── */
const IconPlus = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
  </svg>
);
const IconClose = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);
const IconKey = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
  </svg>
);
const IconToggleOn = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="1" y="5" width="22" height="14" rx="7" ry="7"/>
    <circle cx="16" cy="12" r="3" fill="currentColor"/>
  </svg>
);
const IconToggleOff = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="1" y="5" width="22" height="14" rx="7" ry="7"/>
    <circle cx="8" cy="12" r="3" fill="currentColor"/>
  </svg>
);
const IconSearch = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
);
const IconX = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);


export default function AdminUsers() {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ nom: "", prenom: "", email: "", password: "", role: "AGENT" });
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState<"TOUS" | "ADMIN" | "AGENT">("TOUS");
  const [statutFilter, setStatutFilter] = useState<"TOUS" | "actif" | "inactif">("TOUS");

  const filteredUsers = users.filter(u => {
    const q = search.trim().toLowerCase();
    const matchSearch  = !q || `${u.prenom} ${u.nom}`.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
    const matchRole    = roleFilter === "TOUS" || u.roles.includes(roleFilter);
    const matchStatut  = statutFilter === "TOUS" || (statutFilter === "actif" ? u.actif : !u.actif);
    return matchSearch && matchRole && matchStatut;
  });

  const loadUsers = () => getUsers().then(setUsers).catch(console.error);
  useEffect(() => { loadUsers(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await createUser(form);
      setForm({ nom: "", prenom: "", email: "", password: "", role: "AGENT" });
      setShowForm(false);
      loadUsers();
    } catch (err: any) {
      setError(err.response?.data || "Erreur lors de la création");
    }
  };

  const handleToggle = async (id: number) => {
    await toggleActivation(id);
    loadUsers();
  };

  const handleResetPassword = async (id: number) => {
    const newPass = prompt("Nouveau mot de passe pour cet utilisateur :");
    if (newPass) {
      await resetPassword(id, newPass);
      alert("Mot de passe réinitialisé");
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Gestion des utilisateurs</h2>
          <p className="page-subtitle">{users.length} utilisateur{users.length !== 1 ? "s" : ""}</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? <><IconClose /> Annuler</> : <><IconPlus /> Nouvel utilisateur</>}
        </button>
      </div>

      {/* ── Barre de filtres ── */}
      <div className="users-filters">
        <div className="uf-search-wrap">
          <span className="uf-search-icon"><IconSearch /></span>
          <input
            className="uf-search-input"
            type="text"
            placeholder="Rechercher par nom ou email…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          {search && (
            <button className="uf-search-clear" onClick={() => setSearch("")}
              title="Effacer"><IconX /></button>
          )}
        </div>

        <div className="uf-pills">
          {(["TOUS", "AGENT", "ADMIN"] as const).map(r => (
            <button
              key={r}
              className={`uf-pill${roleFilter === r ? " uf-pill-active" : ""}`}
              onClick={() => setRoleFilter(r)}
            >
              {r === "TOUS" ? "Tous les rôles" : r === "AGENT" ? "Agents" : "Admins"}
              <span className="uf-pill-count">
                {r === "TOUS" ? users.length : users.filter(u => u.roles.includes(r)).length}
              </span>
            </button>
          ))}
        </div>

        <div className="uf-pills">
          {(["TOUS", "actif", "inactif"] as const).map(s => (
            <button
              key={s}
              className={`uf-pill${
                statutFilter === s
                  ? s === "actif" ? " uf-pill-active uf-pill-active-green"
                  : s === "inactif" ? " uf-pill-active uf-pill-active-gray"
                  : " uf-pill-active"
                  : ""
              }`}
              onClick={() => setStatutFilter(s)}
            >
              {s === "TOUS" ? "Tous les statuts" : s === "actif" ? "Actifs" : "Inactifs"}
              <span className="uf-pill-count">
                {s === "TOUS" ? users.length
                  : s === "actif" ? users.filter(u => u.actif).length
                  : users.filter(u => !u.actif).length}
              </span>
            </button>
          ))}
        </div>

        {(search || roleFilter !== "TOUS" || statutFilter !== "TOUS") && (
          <button className="uf-reset" onClick={() => { setSearch(""); setRoleFilter("TOUS"); setStatutFilter("TOUS"); }}>
            <IconX /> Réinitialiser
          </button>
        )}
      </div>

      {/* Compteur résultat */}
      <p className="uf-count">
        {filteredUsers.length} utilisateur{filteredUsers.length !== 1 ? "s" : ""} affiché{filteredUsers.length !== 1 ? "s" : ""}
        {users.length !== filteredUsers.length && <span className="uf-count-total"> sur {users.length}</span>}
      </p>

      {showForm && (
        <form className="user-form" onSubmit={handleCreate}>
          <h3 className="form-title">Créer un utilisateur</h3>
          <div className="form-row">
            <label className="field">
              <span>Nom</span>
              <input placeholder="Dupont" value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} required />
            </label>
            <label className="field">
              <span>Prénom</span>
              <input placeholder="Jean" value={form.prenom} onChange={e => setForm({ ...form, prenom: e.target.value })} required />
            </label>
          </div>
          <div className="form-row">
            <label className="field">
              <span>Email</span>
              <input type="email" placeholder="jean.dupont@eqdom.ma" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
            </label>
            <label className="field">
              <span>Mot de passe</span>
              <input type="password" placeholder="••••••••" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required />
            </label>
          </div>
          <div className="form-row form-row-short">
            <label className="field">
              <span>Rôle</span>
              <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
                <option value="AGENT">Agent</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
            <button type="submit" className="btn-primary btn-submit">Créer l'utilisateur</button>
          </div>
          {error && <div className="form-error">{error}</div>}
        </form>
      )}

      <div className="table-container">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Utilisateur</th>
              <th>Email</th>
              <th>Rôle</th>
              <th>Dossiers</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map(u => (
              <tr key={u.id}>
                <td>
                  <div className="user-cell">
                    <div className="avatar">{u.prenom[0]}{u.nom[0]}</div>
                    <span>{u.prenom} {u.nom}</span>
                  </div>
                </td>
                <td><span className="email-text">{u.email}</span></td>
                <td>
                  <span className={`role-badge ${u.roles.includes("ADMIN") ? "role-admin" : "role-agent"}`}>
                    {u.roles.join(", ")}
                  </span>
                </td>
                <td><strong>{u.nbDossiersAssignes}</strong></td>
                <td>
                  <span className={`badge-statut ${u.actif ? "actif" : "inactif"}`}>
                    {u.actif ? "Actif" : "Désactivé"}
                  </span>
                </td>
                <td className="actions-cell">
                  <button
                    className={u.actif ? "btn-action btn-deactivate" : "btn-action btn-activate"}
                    onClick={() => handleToggle(u.id)}
                    title={u.actif ? "Désactiver" : "Activer"}
                  >
                    {u.actif ? <IconToggleOn /> : <IconToggleOff />}
                    {u.actif ? "Désactiver" : "Activer"}
                  </button>
                  <button className="btn-action btn-reset" onClick={() => handleResetPassword(u.id)} title="Réinitialiser le mot de passe">
                    <IconKey />
                    Reset MDP
                  </button>
                </td>
              </tr>
            ))}
            {filteredUsers.length === 0 && (
              <tr><td colSpan={6} className="users-empty">
                Aucun utilisateur ne correspond aux filtres actifs
              </td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}