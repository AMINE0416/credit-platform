import { useState } from "react";
import { importExcel, resetDatabase } from "../services/importService";
import type { ImportRapport } from "../services/importService";
import "./AdminImport.css";

export default function AdminImport() {
    const [file, setFile] = useState<File | null>(null);
    const [rapport, setRapport] = useState<ImportRapport | null>(null);
    const [loading, setLoading] = useState(false);
    const [resetLoading, setResetLoading] = useState(false);

    const handleImport = async () => {
        if (!file) return;
        setLoading(true);
        setRapport(null);
        try {
            const result = await importExcel(file);
            setRapport(result);
        } catch (e) {
            alert("Erreur lors de l'import");
        } finally {
            setLoading(false);
        }
    };

    const handleReset = async () => {
        const confirmation = prompt(
            "⚠️ ATTENTION : Cette action va SUPPRIMER TOUS les dossiers, crédits, clients et agents.\n\n" +
            "L'admin sera conservé mais toutes les autres données seront perdues.\n\n" +
            "Tapez 'SUPPRIMER' (en majuscules) pour confirmer:"
        );
        
        if (confirmation !== "SUPPRIMER") {
            alert("Annulé - Aucune donnée n'a été supprimée");
            return;
        }
        
        setResetLoading(true);
        try {
            const result = await resetDatabase();
            alert(`✅ Base réinitialisée:\n\n` +
                  `Supprimé:\n` +
                  `- ${result.suppressed.actions} actions\n` +
                  `- ${result.suppressed.dossiers} dossiers\n` +
                  `- ${result.suppressed.credits} crédits\n` +
                  `- ${result.suppressed.clients} clients\n` +
                  `- ${result.suppressed.agents} agents\n\n` +
                  `Vous pouvez maintenant réimporter votre fichier Excel.`
            );
            setRapport(null);
        } catch (e: any) {
            alert("Erreur : " + (e.message || "Erreur inconnue"));
        } finally {
            setResetLoading(false);
        }
    };

    return (
        <div>
            <h2 className="page-title">Import du fichier Excel</h2>

            {/* 🆕 Bouton de réinitialisation de la base */}
            <div className="import-card" style={{ marginBottom: "20px", backgroundColor: "#ffe5e5", borderLeft: "4px solid #dc3545" }}>
                <h3 style={{ color: "#dc3545", display: "flex", alignItems: "center", gap: "8px" }}>
                    ⚠️ Zone Dangereuse
                </h3>
                <p className="import-hint" style={{ color: "#721c24" }}>
                    <strong>Réinitialiser la base de données</strong> supprimera TOUS les dossiers, crédits, clients et agents.
                    L'utilisateur admin sera conservé. Utilisez cette fonction pour repartir de zéro avant un nouvel import.
                </p>
                <button 
                    className="btn-primary" 
                    onClick={handleReset} 
                    disabled={resetLoading}
                    style={{ backgroundColor: "#dc3545", border: "none" }}
                >
                    {resetLoading ? "Suppression en cours..." : "🗑️ Réinitialiser la base de données"}
                </button>
            </div>

            <div className="import-card">
                <p className="import-hint">
                    Le fichier doit contenir les colonnes obligatoires : num_cin1, nom, prenom, cli_tel, TEL2, cli_adr, Lib_Ville, cod_age, lib_age, Adresse_Cadrage, ANC_REFDO, From_Prov, GEST_CIN, CSP_PDT, Segment, nbr_imp, Mensualite, encours, Mnt_Imp, prv_cum, stage, DVR, Reecheance, Immatriculation.
                    <br /><br />
                    <strong>📌 Affectation automatique :</strong> Les dossiers sont automatiquement affectés aux agents lors de l'import. 
                    Si une colonne "Agent_Email" est présente, les dossiers seront assignés à l'agent correspondant (l'agent sera créé automatiquement s'il n'existe pas).
                </p>
                <input type="file" accept=".xlsx,.xls" onChange={e => setFile(e.target.files?.[0] || null)} />
                <button className="btn-primary" onClick={handleImport} disabled={!file || loading}>
                    {loading ? "Import en cours..." : "Lancer l'import"}
                </button>
            </div>

            {rapport && (
                <div className="rapport-card">
                    <h3>Rapport d'import #{rapport.jobId}</h3>
                    <div className="rapport-stats">
                        <div className="stat"><span className="stat-value">{rapport.nbLignesTotal}</span><span>Lignes traitées</span></div>
                        <div className="stat success"><span className="stat-value">{rapport.nbInserees}</span><span>Insérées</span></div>
                        <div className="stat info"><span className="stat-value">{rapport.nbMisesAJour}</span><span>Mises à jour</span></div>
                        <div className="stat error"><span className="stat-value">{rapport.nbRejetees}</span><span>Rejetées</span></div>
                    </div>

                    {rapport.erreurs.length > 0 && (
                        <>
                            <h4>Détail des erreurs</h4>
                            <table className="admin-table">
                                <thead>
                                <tr><th>Ligne</th><th>Référence contrat</th><th>Motif</th></tr>
                                </thead>
                                <tbody>
                                {rapport.erreurs.map((e, i) => (
                                    <tr key={i}>
                                        <td>{e.ligne}</td>
                                        <td>{e.referenceContrat || "-"}</td>
                                        <td className="motif-erreur">{e.motif}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}