import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getDossier, saveAction, getTransitionsAutorisees } from "../services/dossierService";
import { getRoles } from "../services/authService";
import type { DossierDetail as DossierDetailType, TransitionInfo, ChampsRequis } from "../services/dossierService";
import "./DossierDetail.css";
import eqdomLogo from "../assets/eqdom.jpg";
import DatePromesseSelector from "../components/DatePromesseSelector";

export default function DossierDetail() {
    const { id } = useParams();
    const [dossier, setDossier] = useState<DossierDetailType | null>(null);
    const [transitionInfo, setTransitionInfo] = useState<TransitionInfo | null>(null);
    const [champsRequis, setChampsRequis] = useState<ChampsRequis | null>(null);
    const [loadingTransitions, setLoadingTransitions] = useState(false);
    
    // Vérifier si l'utilisateur est ADMIN (lecture seule) ou AGENT (peut saisir)
    const userRoles = getRoles();
    const isAdmin = userRoles.includes("ADMIN");
    const isAgent = userRoles.includes("AGENT");
    const canEdit = isAgent && !isAdmin; // Seuls les agents peuvent modifier

    const [natureAction, setNatureAction] = useState("");
    const [motifImpaye, setMotifImpaye] = useState("");
    const [compteRendu, setCompteRendu] = useState("");
    const [codeResultat, setCodeResultat] = useState("");
    const [recidive, setRecidive] = useState(false);
    const [montantPromesse, setMontantPromesse] = useState("");
    const [datePromesse, setDatePromesse] = useState("");
    const [nouveauTelephone, setNouveauTelephone] = useState("");
    const [nouveauStatut, setNouveauStatut] = useState(""); // Le statut DOIT être choisi en premier
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (id) {
            getDossier(Number(id)).then(setDossier).catch(console.error);
        }
    }, [id]);

    // Charger les transitions autorisées au chargement du dossier (seulement pour les agents)
    useEffect(() => {
        if (id && dossier && canEdit) {
            setLoadingTransitions(true);
            getTransitionsAutorisees(Number(id))
                .then(info => {
                    console.log("Transitions autorisées:", info);
                    setTransitionInfo(info);
                })
                .catch(err => {
                    console.error("Erreur lors du chargement des transitions:", err);
                })
                .finally(() => setLoadingTransitions(false));
        }
    }, [id, dossier?.statut, canEdit]);

    // Charger les champs requis quand le nouveau statut change (seulement pour les agents)
    useEffect(() => {
        if (id && nouveauStatut && canEdit) {
            getTransitionsAutorisees(Number(id), nouveauStatut)
                .then(info => {
                    setChampsRequis(info.champsRequis);
                    
                    // Pré-cocher "Récidive = Oui" si transition INJOIGNABLE → INJOIGNABLE
                    if (dossier?.statut === "INJOIGNABLE" && nouveauStatut === "INJOIGNABLE") {
                        setRecidive(true);
                    }
                })
                .catch(console.error);
        } else {
            setChampsRequis(null);
        }
    }, [id, nouveauStatut, dossier?.statut, canEdit]);

    const handleSave = async () => {
        if (!id || !dossier) return;
        
        // Validation du nouveau statut (obligatoire)
        if (!nouveauStatut) {
            alert("Veuillez sélectionner le nouveau statut du dossier");
            return;
        }
        
        // Validation des champs obligatoires selon les règles métier
        if (!natureAction) {
            alert("Nature Action est obligatoire");
            return;
        }
        if (!codeResultat) {
            alert("Code résultat est obligatoire");
            return;
        }
        
        // Validation du numéro de téléphone marocain si fourni
        if (nouveauTelephone && nouveauTelephone.trim() !== "") {
            const telPattern = /^0[67][0-9]{8}$/;
            if (!telPattern.test(nouveauTelephone)) {
                alert("Format de téléphone invalide.\nLe numéro doit contenir exactement 10 chiffres et commencer par 06 ou 07.\nExemple : 0612345678 ou 0712345678");
                return;
            }
        }
        
        // Validation conditionnelle selon champsRequis
        if (champsRequis) {
            if (champsRequis.compteRenduObligatoire && (!compteRendu || compteRendu.trim() === "")) {
                alert("Compte rendu est obligatoire pour cette transition");
                return;
            }
            if (champsRequis.motifImpayeObligatoire && !motifImpaye) {
                alert("Motif Impayé est obligatoire pour cette transition");
                return;
            }
            if (champsRequis.recidiveObligatoire && recidive === null) {
                alert("Récidive est obligatoire pour cette transition");
                return;
            }
            if (champsRequis.montantPromesseObligatoire && !montantPromesse) {
                alert("Montant de la promesse est obligatoire pour passer en PROMESSE");
                return;
            }
            if (champsRequis.datePromesseObligatoire && !datePromesse) {
                alert("Date de la promesse est obligatoire pour passer en PROMESSE");
                return;
            }
        }
        
        setSaving(true);
        try {
            await saveAction(Number(id), {
                natureAction: natureAction,
                motifImpaye: motifImpaye || "",
                compteRendu: compteRendu.trim() || "",
                codeResultat: codeResultat,
                recidive: recidive ? "OUI" : "NON",
                nbreActionClient: null, // Calculé automatiquement côté backend
                montantPromesse: montantPromesse ? Number(montantPromesse) : null,
                datePromesse: datePromesse || null,
                nouveauTelephone: nouveauTelephone.trim() || null,
                nouveauStatut: nouveauStatut,
            });
            
            alert(`Action enregistrée avec succès\nNouveau statut: ${nouveauStatut}`);
            
            // Réinitialiser le formulaire
            setNatureAction("");
            setMotifImpaye("");
            setCompteRendu("");
            setCodeResultat("");
            setRecidive(false);
            setMontantPromesse("");
            setDatePromesse("");
            setNouveauTelephone("");
            setNouveauStatut("");
            setChampsRequis(null);
            
            // Recharger les données du dossier
            getDossier(Number(id)).then(setDossier);
        } catch (e: any) {
            const errorMsg = e.response?.data;
            // Gérer différents types de réponse d'erreur
            let displayMsg = "Erreur lors de l'enregistrement";
            
            if (typeof errorMsg === 'string') {
                displayMsg = errorMsg;
            } else if (errorMsg && typeof errorMsg === 'object') {
                displayMsg = errorMsg.message || JSON.stringify(errorMsg);
            }
            
            alert(displayMsg);
            console.error("Erreur complète:", e);
            console.error("Réponse backend:", e.response);
        } finally {
            setSaving(false);
        }
    };

    if (!dossier) return <div className="loading">Chargement...</div>;

    const { client, credit } = dossier;
    const isFormDisabled = dossier.statut === "CLOTURE" || !canEdit; // Désactiver pour CLOTURE ou si ADMIN

    return (
        <div className="fiche-page">
            <header className="fiche-header">
                <div className="header-left">
                    <img src={eqdomLogo} alt="eqdom" className="eqdom-logo" />
                    <div className="brand-text">
                        <span className="brand-name">eqdom</span>
                        <span className="brand-tagline">L'essentiel, c'est vous.</span>
                    </div>
                </div>
                <div style={{marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '10px'}}>
                    <span style={{fontWeight: 'bold'}}>Statut actuel:</span>
                    <span style={{
                        padding: '5px 12px',
                        borderRadius: '4px',
                        fontWeight: 'bold',
                        backgroundColor: 
                            dossier.statut === 'NOUVEAU' ? '#3498db' :
                            dossier.statut === 'EN_COURS' ? '#f39c12' :
                            dossier.statut === 'PROMESSE' ? '#9b59b6' :
                            dossier.statut === 'INJOIGNABLE' ? '#e74c3c' :
                            dossier.statut === 'REGULARISE' ? '#2ecc71' :
                            dossier.statut === 'CLOTURE' ? '#95a5a6' : '#666',
                        color: 'white'
                    }}>
                        {dossier.statut}
                    </span>
                    <span style={{color: '#666', fontSize: '0.9em'}}>
                        (Actions: {dossier.nbActions})
                    </span>
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

                    {!canEdit && (
                        <div style={{padding: '10px', backgroundColor: '#fff3cd', border: '1px solid #ffc107', borderRadius: '4px', marginBottom: '15px', color: '#856404'}}>
                            ℹ️ Mode consultation : Les administrateurs ne peuvent que visualiser les dossiers. Seuls les agents peuvent effectuer des saisies.
                        </div>
                    )}

                    {isFormDisabled && dossier.statut === "CLOTURE" && (
                        <div style={{padding: '10px', backgroundColor: '#f8d7da', border: '1px solid #f5c6cb', borderRadius: '4px', marginBottom: '15px', color: '#721c24'}}>
                            ⚠️ Ce dossier est CLOTURÉ, aucune modification n'est possible.
                        </div>
                    )}

                    {/* Afficher le formulaire seulement si l'utilisateur est agent */}
                    {canEdit && (
                        <>
                            {/* NOUVEAU STATUT - À CHOISIR EN PREMIER */}
                            <label>Nouveau statut du dossier <span style={{color: 'red'}}>*</span></label>
                            {loadingTransitions ? (
                                <div style={{padding: '10px', textAlign: 'center', color: '#666'}}>
                                    Chargement des statuts disponibles...
                                </div>
                            ) : (
                                <>
                                    <select 
                                        value={nouveauStatut} 
                                        onChange={(e) => setNouveauStatut(e.target.value)}
                                        disabled={isFormDisabled || !transitionInfo?.transitionsAutorisees || transitionInfo.transitionsAutorisees.length === 0}
                                        required
                                        style={{backgroundColor: nouveauStatut ? '#e8f5e9' : 'white', fontWeight: nouveauStatut ? 'bold' : 'normal'}}
                                    >
                                        <option value="">
                                            {transitionInfo?.transitionsAutorisees && transitionInfo.transitionsAutorisees.length > 0 
                                                ? "-- Sélectionner d'abord le statut --" 
                                                : "Aucune transition disponible"}
                                        </option>
                                        {transitionInfo?.transitionsAutorisees?.map(statut => (
                                            <option key={statut} value={statut}>
                                                {statut}
                                            </option>
                                        ))}
                                    </select>
                                    {transitionInfo && transitionInfo.transitionsAutorisees.length > 0 && (
                                        <small style={{color: '#666', fontSize: '0.85em', display: 'block', marginTop: '5px'}}>
                                            {transitionInfo.transitionsAutorisees.length} transition(s) disponible(s) depuis {dossier.statut}
                                        </small>
                                    )}
                                </>
                            )}
                            <small style={{color: '#666', fontSize: '0.85em', display: 'block', marginBottom: '10px'}}>
                                {!nouveauStatut && "⚠️ Choisissez d'abord vers quel statut vous voulez faire évoluer ce dossier"}
                                {nouveauStatut && `Les champs ci-dessous sont adaptés pour la transition ${dossier.statut} → ${nouveauStatut}`}
                            </small>

                            {/* Afficher le reste du formulaire seulement si un statut est choisi */}
                            {nouveauStatut && champsRequis && (
                                <>
                            {/* NATURE ACTION - Toujours obligatoire */}
                            <label>Nature Action <span style={{color: 'red'}}>*</span></label>
                            <select value={natureAction} onChange={(e) => setNatureAction(e.target.value)} disabled={isFormDisabled} required>
                                <option value="">-- Sélectionner --</option>
                                <option value="APPEL_SORTANT">Appel sortant</option>
                                <option value="APPEL_ENTRANT">Appel entrant (client rappelle)</option>
                                <option value="SMS_RELANCE">SMS de relance</option>
                                <option value="EMAIL_RELANCE">Email de relance</option>
                                <option value="VISITE_TERRAIN">Visite terrain</option>
                                <option value="COURRIER_MISE_EN_DEMEURE">Courrier / mise en demeure</option>
                                <option value="RELANCE_VIA_TIERS">Relance via tiers (garant, entourage)</option>
                            </select>

                            {/* MOTIF IMPAYE - Conditionnel */}
                            {champsRequis.motifImpayeVisible && (
                                <>
                                    <label>Motif Impayé {champsRequis.motifImpayeObligatoire && <span style={{color: 'red'}}>*</span>}</label>
                                    <select value={motifImpaye} onChange={(e) => setMotifImpaye(e.target.value)} disabled={isFormDisabled} required={champsRequis.motifImpayeObligatoire}>
                                        <option value="">-- Sélectionner --</option>
                                        <option value="PERTE_EMPLOI">Perte d'emploi</option>
                                        <option value="DIFFICULTE_FINANCIERE_TEMPORAIRE">Difficulté financière temporaire</option>
                                        <option value="LITIGE_VEHICULE_PRODUIT">Litige sur le véhicule/produit</option>
                                        <option value="DECES_EMPRUNTEUR">Décès de l'emprunteur</option>
                                        <option value="MALADIE_HOSPITALISATION">Maladie / hospitalisation</option>
                                        <option value="OUBLI_PAIEMENT">Oubli de paiement</option>
                                        <option value="CONTESTATION_MONTANT">Contestation du montant</option>
                                        <option value="CLIENT_INJOIGNABLE">Client injoignable (motif inconnu)</option>
                                        <option value="AUTRE">Autre</option>
                                    </select>
                                </>
                            )}

                            {/* COMPTE RENDU - Conditionnel */}
                            <label>Compte rendu {champsRequis.compteRenduObligatoire && <span style={{color: 'red'}}>*</span>}</label>
                            <textarea 
                                value={compteRendu} 
                                onChange={(e) => setCompteRendu(e.target.value)} 
                                rows={3} 
                                disabled={isFormDisabled}
                                required={champsRequis.compteRenduObligatoire}
                                placeholder={champsRequis.compteRenduObligatoire ? "Décrivez l'action effectuée..." : "Optionnel pour cette transition"}
                            />

                            {/* CODE RESULTAT - Toujours obligatoire */}
                            <label>Code resultat <span style={{color: 'red'}}>*</span></label>
                            <select value={codeResultat} onChange={(e) => setCodeResultat(e.target.value)} disabled={isFormDisabled} required>
                                <option value="">-- Sélectionner --</option>
                                <option value="RC">RC - Right Contact</option>
                                <option value="RPC">RPC - Right Party Contact</option>
                                <option value="NRP">NRP - No Right Party</option>
                                <option value="NRC">NRC - No Reply Contact</option>
                                <option value="NUM_INVALIDE">NUM_INVALIDE - Numéro invalide/hors service</option>
                                <option value="REFUS">REFUS - Client refuse de payer</option>
                                <option value="DIFFERE">DIFFERE - Client demande un délai</option>
                                <option value="REGLE">REGLE - Client confirme paiement effectué</option>
                            </select>

                            {/* RECIDIVE - Conditionnel */}
                            {champsRequis.recidiveVisible && (
                                <>
                                    <label>Récidive O/N {champsRequis.recidiveObligatoire && <span style={{color: 'red'}}>*</span>}</label>
                                    <select 
                                        value={recidive ? "O" : "N"} 
                                        onChange={(e) => setRecidive(e.target.value === "O")} 
                                        disabled={isFormDisabled}
                                        required={champsRequis.recidiveObligatoire}
                                    >
                                        <option value="N">Non</option>
                                        <option value="O">Oui</option>
                                    </select>
                                </>
                            )}

                            {/* MONTANT PROMESSE - Conditionnel */}
                            {champsRequis.montantPromesseVisible && (
                                <>
                                    <label>Montant de la promesse {champsRequis.montantPromesseObligatoire && <span style={{color: 'red'}}>*</span>}</label>
                                    <input 
                                        type="number" 
                                        value={montantPromesse} 
                                        onChange={(e) => setMontantPromesse(e.target.value)} 
                                        disabled={isFormDisabled}
                                        required={champsRequis.montantPromesseObligatoire}
                                        placeholder="Ex: 3520" 
                                    />
                                </>
                            )}

                            {/* DATE PROMESSE - Conditionnel - CALENDRIER */}
                            {champsRequis.datePromesseVisible && (
                                <>
                                    <label>Date de la promesse {champsRequis.datePromesseObligatoire && <span style={{color: 'red'}}>*</span>}</label>
                                    <DatePromesseSelector
                                        value={datePromesse}
                                        onChange={setDatePromesse}
                                        disabled={isFormDisabled}
                                        required={champsRequis.datePromesseObligatoire}
                                    />
                                </>
                            )}

                            {/* NOUVEAU TELEPHONE - Validation Maroc : 10 chiffres commençant par 06 ou 07 */}
                            {champsRequis.nouveauTelephoneVisible && (
                                <>
                                    <label>Nouveau téléphone qualifié</label>
                                    <input 
                                        value={nouveauTelephone} 
                                        onChange={(e) => {
                                            const value = e.target.value.replace(/[^0-9]/g, ''); // Garder seulement les chiffres
                                            if (value.length <= 10) {
                                                setNouveauTelephone(value);
                                            }
                                        }} 
                                        disabled={isFormDisabled}
                                        placeholder="Ex: 0612345678"
                                        maxLength={10}
                                        pattern="0[67][0-9]{8}"
                                        title="Le numéro doit contenir 10 chiffres et commencer par 06 ou 07"
                                    />
                                    <small style={{
                                        color: nouveauTelephone && !nouveauTelephone.match(/^0[67][0-9]{8}$/) ? 'red' : '#666', 
                                        fontSize: '0.85em', 
                                        display: 'block', 
                                        marginTop: '5px'
                                    }}>
                                        {nouveauTelephone && !nouveauTelephone.match(/^0[67][0-9]{8}$/) 
                                            ? '⚠️ Format invalide : 10 chiffres commençant par 06 ou 07' 
                                            : '📱 Format : 0612345678 ou 0712345678 (10 chiffres)'}
                                    </small>
                                </>
                            )}

                            {/* BOUTON ENREGISTRER - juste après le téléphone */}
                            <button 
                                className="btn-enregistrer-inline" 
                                onClick={handleSave} 
                                disabled={saving || isFormDisabled || !nouveauStatut}
                            >
                                {saving ? "Enregistrement..." : "Enregistrer"}
                            </button>
                                </>
                            )}
                        </>
                    )}

                    {/* Message pour les admins */}
                    {!canEdit && (
                        <div style={{padding: '20px', textAlign: 'center', color: '#666'}}>
                            <p>Consultez l'historique des actions dans l'onglet approprié.</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Actions en bas supprimées car bouton est maintenant intégré dans la colonne */}
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
