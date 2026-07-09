import { useState } from "react";
import { importExcel } from "../services/importService";
import type { ImportRapport } from "../services/importService";
import "./AdminImport.css";

export default function AdminImport() {
    const [file, setFile] = useState<File | null>(null);
    const [rapport, setRapport] = useState<ImportRapport | null>(null);
    const [loading, setLoading] = useState(false);

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

    return (
        <div>
            <h2 className="page-title">Import du fichier Excel</h2>

            <div className="import-card">
                <p className="import-hint">
                    Le fichier doit contenir les colonnes obligatoires : cin, nom, prenom, telephone, adresse,
                    ville, agence, reference_contrat, type_credit, segment, mensualite, encours, impaye_montant,
                    impaye_nb_echeances, stage.
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