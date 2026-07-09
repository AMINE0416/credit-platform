import { useEffect, useState } from "react";
import { getUsers, createUser, toggleActivation, resetPassword } from "../services/userService";
import type { UserDTO } from "../services/userService";
import "./AdminUsers.css";

export default function AdminUsers() {
    const [users, setUsers] = useState<UserDTO[]>([]);
    const [showForm, setShowForm] = useState(false);
    const [form, setForm] = useState({ nom: "", prenom: "", email: "", password: "", role: "AGENT" });
    const [error, setError] = useState("");

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
                <h2 className="page-title">Gestion des utilisateurs</h2>
                <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
                    {showForm ? "Annuler" : "+ Nouvel utilisateur"}
                </button>
            </div>

            {showForm && (
                <form className="user-form" onSubmit={handleCreate}>
                    <div className="form-row">
                        <input placeholder="Nom" value={form.nom} onChange={e => setForm({...form, nom: e.target.value})} required />
                        <input placeholder="Prénom" value={form.prenom} onChange={e => setForm({...form, prenom: e.target.value})} required />
                    </div>
                    <div className="form-row">
                        <input type="email" placeholder="Email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} required />
                        <input type="password" placeholder="Mot de passe" value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
                    </div>
                    <div className="form-row">
                        <select value={form.role} onChange={e => setForm({...form, role: e.target.value})}>
                            <option value="AGENT">Agent</option>
                            <option value="ADMIN">Admin</option>
                        </select>
                        <button type="submit" className="btn-primary">Créer</button>
                    </div>
                    {error && <div className="form-error">{error}</div>}
                </form>
            )}

            <table className="admin-table">
                <thead>
                <tr>
                    <th>Nom complet</th>
                    <th>Email</th>
                    <th>Rôle</th>
                    <th>Dossiers assignés</th>
                    <th>Statut</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map(u => (
                    <tr key={u.id}>
                        <td>{u.prenom} {u.nom}</td>
                        <td>{u.email}</td>
                        <td>{u.roles.join(", ")}</td>
                        <td>{u.nbDossiersAssignes}</td>
                        <td>
                <span className={`badge-statut ${u.actif ? "actif" : "inactif"}`}>
                  {u.actif ? "Actif" : "Désactivé"}
                </span>
                        </td>
                        <td className="actions-cell">
                            <button onClick={() => handleToggle(u.id)}>
                                {u.actif ? "Désactiver" : "Activer"}
                            </button>
                            <button onClick={() => handleResetPassword(u.id)}>Reset mdp</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}