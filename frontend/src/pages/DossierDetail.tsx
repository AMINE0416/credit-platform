import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getDossier, saveAction } from "../services/dossierService";
import type { DossierDetail as DossierDetailType } from "../services/dossierService";
import "./DossierDetail.css";
import eqdomLogo from "../assets/eqdom.jpg";

export default function DossierDetail() {
    const { id } = useParams();
    const [dossier, setDossier] = useState<DossierDetailType | null>(null);

    const [natureAction, setNatureAction] = useState("");
    const [motifImpaye, setMotifImpaye] = useState("");
    const [compteRendu, setCompteRendu] = useState("");
    const [codeResultat, setCodeResultat] = useState("");
    const [recidive, setRecidive] = useState(false);
    const [nbreActionClient, setNbreActionClient] = useState("");
    const [montantPromesse, setMontantPromesse] = useState("");
    const [datePromesse, setDatePromesse] = useState("");
    const [nouveauTelephone, setNouveauTelephone] = useState("");
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (id) getDossier(Number(id)).then(setDossier).catch(console.error);
    }, [id]);

    const handleSave = async () => {
        if (!id) return;
        setSaving(true);
        try {
            await saveAction(Number(id), {
                natureAction, motifImpaye, compteRendu, codeResultat,
                recidive, nbreActionClient: Number(nbreActionClient) || null,
                montantPromesse: montantPromesse ? Number(montantPromesse) : null,
                datePromesse: datePromesse || null,
                nouveauTelephone: nouveauTelephone || null,
                nouveauStatut: montantPromesse && datePromesse ? "PROMESSE" : "EN_COURS",
            });
            alert("Action enregistrée avec succès");
            getDossier(Number(id)).then(setDossier);
        } catch (e) {
            alert("Erreur lors de l'enregistrement");
        } finally {
            setSaving(false);
        }
    };

    if (!dossier) return <div className="loading">Chargement...</div>;

    const { client, credit } = dossier;

    return (
        <div className="fiche-page">
            <header className="fiche-header">
                <span>Centre_Aouad</span>
                <div className="fiche-header-logo">

                    <img className="logo-image" src={eqdomLogo} alt="Logo Eqdom" />
                    <div className="logo-text"><strong>eqdom</strong><small>L'essentiel, c'est vous.</small></div>
                </div>
            </header>

            <div className="fiche-body">
                {/* Colonne 1 : Données Client */}
                <div className="fiche-col">
                    <h3>Données Client</h3>
                    <Field label="num_cin1" value={client.cin} />
                    <Field label="nom_pre" value={`${client.prenom} ${client.nom}`} />
                    <Field label="cli_tel" value={client.telephone} />
                    <Field label="TEL2" value={client.telephone2} />
                    <Field label="cli_adr" value={client.adresse} />
                    <Field label="Lib_Ville" value={client.ville} />
                    <Field label="cod_age" value={client.codeAgence} />
                    <Field label="lib_age" value={client.libAgence} />
                    <Field label="Adresse Cadrage" value={client.adresseCadrage || ""} textarea />
                </div>

                {/* Colonne 2 : Données Crédit */}
                <div className="fiche-col">
                    <h3>Données Credit</h3>
                    <Field label="ANC_REFDO" value={credit.referenceContrat} />
                    <Field label="From_Prov" value={credit.fromProv} />
                    <Field label="GEST CIN" value={credit.gestCin} />
                    <Field label="CSP/PDT" value={credit.typeCredit} />
                    <Field label="Segment" value={credit.segment} />
                    <Field label="nbr_imp" value={String(credit.nbrImpaye ?? "")} />
                    <Field label="Mensualite" value={String(credit.mensualite ?? "")} />
                    <Field label="encours" value={String(credit.encours ?? "")} />
                    <Field label="Mnt Imp" value={String(credit.impayeMontant ?? "")} />
                    <Field label="prv_cum" value={String(credit.prvCum ?? "")} />
                    <Field label="stage" value={credit.stage} />
                    <Field label="DVR" value={credit.dvr || ""} />
                    <Field label="Réécheance" value={credit.reecheance || ""} />
                    <Field label="Immatriculatio" value={credit.immatriculation || ""} />
                </div>

                {/* Colonne 3 : Données à Saisir */}
                <div className="fiche-col fiche-col-input">
                    <h3>Données à Saisir</h3>

                    <label>Nature Action</label>
                    <input value={natureAction} onChange={(e) => setNatureAction(e.target.value)} />

                    <label>Motif Impayé</label>
                    <input value={motifImpaye} onChange={(e) => setMotifImpaye(e.target.value)} />

                    <label>Compte rendu</label>
                    <textarea value={compteRendu} onChange={(e) => setCompteRendu(e.target.value)} rows={3} />

                    <label>Code resultat</label>
                    <input value={codeResultat} onChange={(e) => setCodeResultat(e.target.value)} />

                    <label>Récidive O/N</label>
                    <select value={recidive ? "O" : "N"} onChange={(e) => setRecidive(e.target.value === "O")}>
                        <option value="N">Non</option>
                        <option value="O">Oui</option>
                    </select>

                    <label>Nbre Action Client</label>
                    <input type="number" value={nbreActionClient} onChange={(e) => setNbreActionClient(e.target.value)} />

                    <label>Montant de la promesse</label>
                    <input type="number" value={montantPromesse} onChange={(e) => setMontantPromesse(e.target.value)} />

                    <label>Date de la promesse</label>
                    <input type="date" value={datePromesse} onChange={(e) => setDatePromesse(e.target.value)} />

                    <label>Nouveau téléphone qualifié</label>
                    <input value={nouveauTelephone} onChange={(e) => setNouveauTelephone(e.target.value)} />
                </div>
            </div>

            <div className="fiche-actions">
                <button className="btn-enregistrer" onClick={handleSave} disabled={saving}>
                    {saving ? "Enregistrement..." : "Enregistrer"}
                </button>
                <button className="btn-exporter">Exporter</button>
            </div>
        </div>
    );
}

function Field({ label, value, textarea = false }: { label: string; value: string; textarea?: boolean }) {
    return (
        <div className="field-group">
            <label>{label}</label>
            {textarea ? (
                <textarea value={value} readOnly rows={3} />
            ) : (
                <input value={value} readOnly />
            )}
        </div>
    );
}