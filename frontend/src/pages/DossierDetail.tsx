import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  getDossier,
  saveAction,
  getTransitionsAutorisees,
  getDernierePromesse,
} from "../services/dossierService";
import { getRoles } from "../services/authService";
import type {
  DossierDetail as DossierDetailType,
  TransitionInfo,
  ChampsRequis,
  DernierePromesse,
} from "../services/dossierService";
import "./DossierDetail.css";
import eqdomLogo from "../assets/eqdom.jpg";

/* ─── Toast Notification ──────────────────────────────────── */

type ToastType = "success" | "error" | "warning" | "info";

interface Toast {
  id: number;
  type: ToastType;
  message: string;
}

function ToastContainer({
  toasts,
  remove,
}: {
  toasts: Toast[];
  remove: (id: number) => void;
}) {
  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          <span className="toast-icon">
            {t.type === "success"
              ? "✓"
              : t.type === "error"
                ? "✕"
                : t.type === "warning"
                  ? "⚠"
                  : "ℹ"}
          </span>
          <span className="toast-msg">{t.message}</span>
          <button className="toast-close" onClick={() => remove(t.id)}>
            ×
          </button>
        </div>
      ))}
    </div>
  );
}

function useToast() {
  const [toasts, setToasts] = useState<Toast[]>([]);
  let counter = 0;
  const add = (type: ToastType, message: string) => {
    const id = Date.now() + counter++;
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => remove(id), 5000);
  };
  const remove = (id: number) =>
    setToasts((prev) => prev.filter((t) => t.id !== id));
  return {
    toasts,
    remove,
    success: (m: string) => add("success", m),
    error: (m: string) => add("error", m),
    warning: (m: string) => add("warning", m),
    info: (m: string) => add("info", m),
  };
}

/* ─── Status config ───────────────────────────────────────── */

const STATUT_CONFIG: Record<
  string,
  { color: string; bg: string; label: string }
> = {
  NOUVEAU: { color: "#1d4ed8", bg: "#dbeafe", label: "Nouveau" },
  EN_COURS: { color: "#92400e", bg: "#fef3c7", label: "En cours" },
  PROMESSE: { color: "#6b21a8", bg: "#f3e8ff", label: "Promesse" },
  INJOIGNABLE: { color: "#991b1b", bg: "#fee2e2", label: "Injoignable" },
  REGULARISE: { color: "#14532d", bg: "#dcfce7", label: "Régularisé" },
  CLOTURE: { color: "#374151", bg: "#f3f4f6", label: "Clôturé" },
  ALERTE: { color: "#b91c1c", bg: "#fee2e2", label: "Alerte" },
  SUIVI_PROMESSE: { color: "#7c3aed", bg: "#ede9fe", label: "Suivi Promesse" },
  SUIVI_INJOIGNABLE: {
    color: "#b91c1c",
    bg: "#fecaca",
    label: "Suivi Injoignable",
  },
};

/* ─── Field labels mapping ────────────────────────────────── */

const CLIENT_LABELS: Record<string, string> = {
  cin: "CIN",
  name: "Nom & Prénom",
  telephone: "Téléphone principal",
  telephone2: "Téléphone secondaire",
  adresse: "Adresse",
  ville: "Ville",
  codeAgence: "Code agence",
  libAgence: "Libellé agence",
  adresseCadrage: "Adresse de cadrage",
};

const CREDIT_LABELS: Record<string, string> = {
  referenceContrat: "Réf. contrat",
  fromProv: "Provenance",
  gestCin: "CIN gestionnaire",
  typeCredit: "Type de crédit",
  segment: "Segment",
  nbrImpaye: "Nombre d'impayés",
  mensualite: "Mensualité (MAD)",
  encours: "Encours (MAD)",
  impayeMontant: "Montant impayé (MAD)",
  prvCum: "Provision cumulée",
  stage: "Stage",
  dvr: "DVR",
  reecheance: "Réécheance",
  immatriculation: "Immatriculation",
};

/* ─── SVG Icons ───────────────────────────────────────────── */

const IconUser = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

const IconCreditCard = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
    <line x1="1" y1="10" x2="23" y2="10" />
  </svg>
);

const IconEdit = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
  </svg>
);

const IconSave = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2.5"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
    <polyline points="17 21 17 13 7 13 7 21" />
    <polyline points="7 3 7 8 15 8" />
  </svg>
);

const IconLoader = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2.5"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="spin"
  >
    <path d="M21 12a9 9 0 1 1-6.219-8.56" />
  </svg>
);

const IconArrow = () => (
  <svg
    width="12"
    height="12"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2.5"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <line x1="5" y1="12" x2="19" y2="12" />
    <polyline points="12 5 19 12 12 19" />
  </svg>
);

const IconInfo = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="16" x2="12" y2="12" />
    <line x1="12" y1="8" x2="12.01" y2="8" />
  </svg>
);

const IconLock = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
  </svg>
);

const IconCalendar = () => (
  <svg
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <rect width="18" height="18" x="3" y="4" rx="2" ry="2" />
    <line x1="16" x2="16" y1="2" y2="6" />
    <line x1="8" x2="8" y1="2" y2="6" />
    <line x1="3" x2="21" y1="10" y2="10" />
    <path d="M8 14h.01" />
    <path d="M12 14h.01" />
    <path d="M16 14h.01" />
    <path d="M8 18h.01" />
    <path d="M12 18h.01" />
    <path d="M16 18h.01" />
  </svg>
);

/* ─── Promesse Banner ─────────────────────────────────────── */

interface PromesseBannerProps {
  statut: "PROMESSE" | "SUIVI_PROMESSE";
  montantPromesse: number;
  datePromesse: string;
}

function PromesseBanner({
  statut,
  montantPromesse,
  datePromesse,
}: PromesseBannerProps) {
  const title =
    statut === "PROMESSE"
      ? "Promesse de paiement enregistrée"
      : "Promesse de paiement en cours de suivi";

  return (
    <div className="dd-promesse-alert">
      <div className="dd-promesse-inner">
        <div className="dd-promesse-main">
          <div className="dd-promesse-icon">
            <IconCalendar />
          </div>
          <div className="dd-promesse-title-block">
            <h2 className="dd-promesse-title">{title}</h2>
            <div className="dd-promesse-data">
              <div className="dd-promesse-metric">
                <span className="dd-promesse-metric-label">Montant:</span>
                <div className="dd-promesse-metric-value">
                  <span className="dd-promesse-metric-amount">
                    {montantPromesse.toLocaleString("fr-MA")}
                  </span>
                  <span className="dd-promesse-metric-currency">MAD</span>
                </div>
              </div>
              
              <div className="dd-promesse-metric">
                <span className="dd-promesse-metric-label">Date promesse:</span>
                <div className="dd-promesse-metric-value">
                  <span className="dd-promesse-metric-date">
                    {new Date(datePromesse).toLocaleDateString("fr-FR")}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ─── Main Component ──────────────────────────────────────── */

export default function DossierDetail() {
  const { id } = useParams<{ id: string }>();
  const toast = useToast();
  const [dossier, setDossier] = useState<DossierDetailType | null>(null);
  const [transitionInfo, setTransitionInfo] = useState<TransitionInfo | null>(
    null,
  );
  const [champsRequis, setChampsRequis] = useState<ChampsRequis | null>(null);
  const [loadingTransitions, setLoadingTransitions] = useState(false);
  const [dernierePromesse, setDernierePromesse] =
    useState<DernierePromesse | null>(null);

  const userRoles = getRoles();
  const isAdmin = userRoles.includes("ADMIN");
  const isAgent = userRoles.includes("AGENT");
  const canEdit = isAgent && !isAdmin;

  const [natureAction, setNatureAction] = useState("");
  const [motifImpaye, setMotifImpaye] = useState("");
  const [compteRendu, setCompteRendu] = useState("");
  const [codeResultat, setCodeResultat] = useState("");
  const [recidive, setRecidive] = useState(false);
  const [montantPromesse, setMontantPromesse] = useState("");
  const [datePromesse, setDatePromesse] = useState("");
  const [nouveauTelephone, setNouveauTelephone] = useState("");
  const [nouvelleAdresseCadrage, setNouvelleAdresseCadrage] = useState("");
  const [nouveauStatut, setNouveauStatut] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (id) {
      console.log("Chargement du dossier:", id);
      getDossier(Number(id))
        .then((data) => {
          console.log("Dossier chargé:", data);
          setDossier(data);
        })
        .catch((err) => {
          console.error("Erreur chargement dossier:", err);
          toast.error("Erreur lors du chargement du dossier");
        });
    }
  }, [id]);

  useEffect(() => {
    if (id && dossier?.statut === "SUIVI_PROMESSE") {
      getDernierePromesse(Number(id))
        .then((data) => setDernierePromesse(data))
        .catch((err) => console.error("Erreur chargement promesse:", err));
    } else {
      setDernierePromesse(null);
    }
  }, [id, dossier?.statut]);

  useEffect(() => {
    if (id && dossier && canEdit) {
      setLoadingTransitions(true);
      getTransitionsAutorisees(Number(id))
        .then(setTransitionInfo)
        .catch((err) => console.error("Erreur transitions:", err))
        .finally(() => setLoadingTransitions(false));
    }
  }, [id, dossier?.statut, canEdit]);

  useEffect(() => {
    if (id && nouveauStatut && canEdit) {
      getTransitionsAutorisees(Number(id), nouveauStatut)
        .then((info) => {
          setChampsRequis(info.champsRequis);
          if (
            dossier?.statut === "INJOIGNABLE" &&
            nouveauStatut === "INJOIGNABLE"
          )
            setRecidive(true);
        })
        .catch(console.error);
    } else {
      setChampsRequis(null);
    }
  }, [id, nouveauStatut, dossier?.statut, canEdit]);

  const resetForm = () => {
    setNatureAction("");
    setMotifImpaye("");
    setCompteRendu("");
    setCodeResultat("");
    setRecidive(false);
    setMontantPromesse("");
    setDatePromesse("");
    setNouveauTelephone("");
    setNouvelleAdresseCadrage("");
    setNouveauStatut("");
    setChampsRequis(null);
  };

  const handleSave = async () => {
    if (!id || !dossier) return;

    if (!nouveauStatut) {
      toast.warning("Veuillez sélectionner le nouveau statut du dossier");
      return;
    }
    if (!natureAction) {
      toast.warning("Nature Action est obligatoire");
      return;
    }
    if (!codeResultat) {
      toast.warning("Code résultat est obligatoire");
      return;
    }

    if (nouveauTelephone && nouveauTelephone.trim() !== "") {
      if (!/^0[67][0-9]{8}$/.test(nouveauTelephone)) {
        toast.warning(
          "Format de téléphone invalide. 10 chiffres commençant par 06 ou 07 (ex: 0612345678)",
        );
        return;
      }
    }

    if (champsRequis) {
      if (champsRequis.compteRenduObligatoire && !compteRendu.trim()) {
        toast.warning("Compte rendu est obligatoire pour cette transition");
        return;
      }
      if (champsRequis.motifImpayeObligatoire && !motifImpaye) {
        toast.warning("Motif Impayé est obligatoire pour cette transition");
        return;
      }
      if (champsRequis.montantPromesseObligatoire && !montantPromesse) {
        toast.warning("Montant de la promesse est obligatoire");
        return;
      }
      if (champsRequis.datePromesseObligatoire && !datePromesse) {
        toast.warning("Date de la promesse est obligatoire");
        return;
      }
    }

    setSaving(true);
    try {
      await saveAction(Number(id), {
        natureAction,
        motifImpaye: motifImpaye || "",
        compteRendu: compteRendu.trim() || "",
        codeResultat,
        recidive: recidive ? "OUI" : "NON",
        nbreActionClient: null,
        montantPromesse: montantPromesse ? Number(montantPromesse) : null,
        datePromesse: datePromesse || null,
        nouveauTelephone: nouveauTelephone.trim() || null,
        nouvelleAdresseCadrage: nouvelleAdresseCadrage.trim() || null,
        nouveauStatut,
      });

      toast.success(
        `Action enregistrée avec succès — Nouveau statut : ${STATUT_CONFIG[nouveauStatut]?.label ?? nouveauStatut}`,
      );
      resetForm();
      getDossier(Number(id)).then(setDossier);
    } catch (e: any) {
      const err = e.response?.data;
      const msg =
        typeof err === "string"
          ? err
          : (err?.message ?? "Erreur lors de l'enregistrement");
      toast.error(msg);
      console.error(e);
    } finally {
      setSaving(false);
    }
  };

  if (!dossier)
    return (
      <div className="dd-loading">
        <IconLoader />
        <span>Chargement du dossier…</span>
      </div>
    );

  const { client, credit } = dossier;
  const isFormDisabled = dossier.statut === "CLOTURE" || !canEdit;
  const statutCfg = STATUT_CONFIG[dossier.statut] ?? {
    color: "#374151",
    bg: "#f3f4f6",
    label: dossier.statut,
  };

  return (
    <div className="dd-page">
      <ToastContainer toasts={toast.toasts} remove={toast.remove} />

      {/* ── Header ─────────────────────────────────────────── */}
      <header className="dd-header">
        <div className="dd-header-brand">
          <img src={eqdomLogo} alt="eqdom" className="dd-logo" />
          <div className="dd-brand-text">
            <span className="dd-brand-name">eqdom</span>
            <span className="dd-brand-tagline">L'essentiel, c'est vous.</span>
          </div>
        </div>

        <div className="dd-header-meta">
          <div className="dd-meta-chip">
            <span className="dd-meta-label">Dossier</span>
            <span className="dd-meta-value">#{dossier.dossierId ?? id}</span>
          </div>
          <div className="dd-meta-chip">
            <span className="dd-meta-label">Actions</span>
            <span className="dd-meta-value">{dossier.nbActions}</span>
          </div>
          <div
            className="dd-statut-badge"
            style={{ color: statutCfg.color, background: statutCfg.bg }}
          >
            <span
              className="dd-statut-dot"
              style={{ background: statutCfg.color }}
            />
            {statutCfg.label}
          </div>
        </div>
      </header>

      {/* ── Encadré Promesse (PROMESSE ou SUIVI_PROMESSE) ────── */}
      {(dossier.statut === "PROMESSE" || dossier.statut === "SUIVI_PROMESSE") &&
        dossier.datePromesse &&
        dossier.montantPromesse && (
          <PromesseBanner
            statut={dossier.statut as "PROMESSE" | "SUIVI_PROMESSE"}
            montantPromesse={dossier.montantPromesse}
            datePromesse={dossier.datePromesse}
          />
        )}

      {/* ── Encadré Promesse depuis action (SUIVI_PROMESSE) ───── */}
      {dossier.statut === "SUIVI_PROMESSE" &&
        dernierePromesse &&
        !dossier.datePromesse && (
          <PromesseBanner
            statut="SUIVI_PROMESSE"
            montantPromesse={dernierePromesse.montantPromesse}
            datePromesse={dernierePromesse.datePromesse}
          />
        )}

      {/* ── Body ───────────────────────────────────────────── */}
      <div className="dd-body">
        {/* Colonne 1 — Client */}
        <div className="dd-col dd-col-client">
          <div className="dd-col-header">
            <span className="dd-col-icon dd-col-icon-client">
              <IconUser />
            </span>
            <h3>Données Client</h3>
          </div>
          <div className="dd-fields">
            <Field label={CLIENT_LABELS["cin"]} value={client.cin} />
            <Field
              label={CLIENT_LABELS["name"]}
              value={`${client.prenom} ${client.nom}`}
            />
            <Field
              label={CLIENT_LABELS["telephone"]}
              value={client.telephone}
            />
            <Field
              label={CLIENT_LABELS["telephone2"]}
              value={client.telephone2}
            />
            <Field label={CLIENT_LABELS["adresse"]} value={client.adresse} />
            <Field label={CLIENT_LABELS["ville"]} value={client.ville} />
            <Field
              label={CLIENT_LABELS["codeAgence"]}
              value={client.codeAgence}
            />
            <Field
              label={CLIENT_LABELS["libAgence"]}
              value={client.libAgence}
            />
            <Field
              label={CLIENT_LABELS["adresseCadrage"]}
              value={client.adresseCadrage || "—"}
              textarea
            />
          </div>
        </div>

        {/* Colonne 2 — Crédit */}
        <div className="dd-col dd-col-credit">
          <div className="dd-col-header">
            <span className="dd-col-icon dd-col-icon-credit">
              <IconCreditCard />
            </span>
            <h3>Données Crédit</h3>
          </div>
          <div className="dd-fields">
            <Field
              label={CREDIT_LABELS["referenceContrat"]}
              value={credit.referenceContrat}
            />
            <Field label={CREDIT_LABELS["fromProv"]} value={credit.fromProv} />
            <Field label={CREDIT_LABELS["gestCin"]} value={credit.gestCin} />
            <Field
              label={CREDIT_LABELS["typeCredit"]}
              value={credit.typeCredit}
            />
            <Field label={CREDIT_LABELS["segment"]} value={credit.segment} />
            <Field
              label={CREDIT_LABELS["nbrImpaye"]}
              value={String(credit.nbrImpaye ?? "—")}
              highlight
            />
            <Field
              label={CREDIT_LABELS["mensualite"]}
              value={
                credit.mensualite
                  ? `${Number(credit.mensualite).toLocaleString("fr-MA")} MAD`
                  : "—"
              }
            />
            <Field
              label={CREDIT_LABELS["encours"]}
              value={
                credit.encours
                  ? `${Number(credit.encours).toLocaleString("fr-MA")} MAD`
                  : "—"
              }
            />
            <Field
              label={CREDIT_LABELS["impayeMontant"]}
              value={
                credit.impayeMontant
                  ? `${Number(credit.impayeMontant).toLocaleString("fr-MA")} MAD`
                  : "—"
              }
              highlight
            />
            <Field
              label={CREDIT_LABELS["prvCum"]}
              value={
                credit.prvCum
                  ? `${Number(credit.prvCum).toLocaleString("fr-MA")} MAD`
                  : "—"
              }
            />
            <Field label={CREDIT_LABELS["stage"]} value={credit.stage} />
            <Field label={CREDIT_LABELS["dvr"]} value={credit.dvr || "—"} />
            <Field
              label={CREDIT_LABELS["reecheance"]}
              value={credit.reecheance || "—"}
            />
            <Field
              label={CREDIT_LABELS["immatriculation"]}
              value={credit.immatriculation || "—"}
            />
          </div>
        </div>

        {/* Colonne 3 — Saisie */}
        <div className="dd-col dd-col-input">
          <div className="dd-col-header">
            <span className="dd-col-icon dd-col-icon-input">
              <IconEdit />
            </span>
            <h3>Données à Saisir</h3>
          </div>

          {/* Bandeaux état */}
          {!canEdit && (
            <div className="dd-alert dd-alert-info">
              <IconInfo />
              <span>
                Mode consultation — Les administrateurs ne peuvent que
                visualiser. Seuls les agents effectuent des saisies.
              </span>
            </div>
          )}
          {dossier.statut === "CLOTURE" && (
            <div className="dd-alert dd-alert-error">
              <IconLock />
              <span>
                Ce dossier est <strong>clôturé</strong> — aucune modification
                n'est possible.
              </span>
            </div>
          )}

          {canEdit && (
            <div className="dd-form-area">
              {/* Nouveau statut */}
              <div className="dd-field-wrap">
                <label className="dd-label dd-label-required">
                  Nouveau statut du dossier
                </label>
                {loadingTransitions ? (
                  <div className="dd-skeleton" />
                ) : (
                  <select
                    className="dd-select"
                    value={nouveauStatut}
                    onChange={(e) => setNouveauStatut(e.target.value)}
                    disabled={
                      isFormDisabled ||
                      !transitionInfo?.transitionsAutorisees?.length
                    }
                    style={
                      nouveauStatut
                        ? {
                            borderColor: "#16a34a",
                            background: "#f0fdf4",
                            fontWeight: 600,
                          }
                        : {}
                    }
                  >
                    <option value="">
                      {transitionInfo?.transitionsAutorisees?.length
                        ? "— Sélectionner le statut cible —"
                        : "Aucune transition disponible"}
                    </option>
                    {transitionInfo?.transitionsAutorisees?.map((s) => (
                      <option key={s} value={s}>
                        {STATUT_CONFIG[s]?.label ?? s}
                      </option>
                    ))}
                  </select>
                )}
                {nouveauStatut ? (
                  <p className="dd-hint dd-hint-success">
                    <IconArrow />
                    Transition : <strong>{statutCfg.label}</strong> →{" "}
                    <strong>
                      {STATUT_CONFIG[nouveauStatut]?.label ?? nouveauStatut}
                    </strong>
                  </p>
                ) : (
                  <p className="dd-hint">
                    Choisissez d'abord vers quel statut évoluer ce dossier
                  </p>
                )}
              </div>

              {/* Formulaire conditionnel */}
              {nouveauStatut && champsRequis && (
                <>
                  {/* Nature Action */}
                  <div className="dd-field-wrap">
                    <label className="dd-label dd-label-required">
                      Nature de l'action
                    </label>
                    <select
                      className="dd-select"
                      value={natureAction}
                      onChange={(e) => setNatureAction(e.target.value)}
                      disabled={isFormDisabled}
                      required
                    >
                      <option value="">— Sélectionner —</option>
                      <option value="APPEL_SORTANT">Appel sortant</option>
                      <option value="APPEL_ENTRANT">
                        Appel entrant (client rappelle)
                      </option>
                      <option value="SMS_RELANCE">SMS de relance</option>
                      <option value="EMAIL_RELANCE">Email de relance</option>
                      <option value="VISITE_TERRAIN">Visite terrain</option>
                      <option value="COURRIER_MISE_EN_DEMEURE">
                        Courrier / mise en demeure
                      </option>
                      <option value="RELANCE_VIA_TIERS">
                        Relance via tiers (garant, entourage)
                      </option>
                    </select>
                  </div>

                  {/* Motif Impayé */}
                  {champsRequis.motifImpayeVisible && (
                    <div className="dd-field-wrap">
                      <label
                        className={`dd-label${champsRequis.motifImpayeObligatoire ? " dd-label-required" : ""}`}
                      >
                        Motif impayé
                      </label>
                      <select
                        className="dd-select"
                        value={motifImpaye}
                        onChange={(e) => setMotifImpaye(e.target.value)}
                        disabled={isFormDisabled}
                        required={champsRequis.motifImpayeObligatoire}
                      >
                        <option value="">— Sélectionner —</option>
                        <option value="PERTE_EMPLOI">Perte d'emploi</option>
                        <option value="DIFFICULTE_FINANCIERE_TEMPORAIRE">
                          Difficulté financière temporaire
                        </option>
                        <option value="LITIGE_VEHICULE_PRODUIT">
                          Litige sur le véhicule / produit
                        </option>
                        <option value="DECES_EMPRUNTEUR">
                          Décès de l'emprunteur
                        </option>
                        <option value="MALADIE_HOSPITALISATION">
                          Maladie / hospitalisation
                        </option>
                        <option value="OUBLI_PAIEMENT">Oubli de paiement</option>
                        <option value="CONTESTATION_MONTANT">
                          Contestation du montant
                        </option>
                        <option value="CLIENT_INJOIGNABLE">
                          Client injoignable (motif inconnu)
                        </option>
                        <option value="AUTRE">Autre</option>
                      </select>
                    </div>
                  )}

                  {/* Compte rendu */}
                  <div className="dd-field-wrap">
                    <label
                      className={`dd-label${champsRequis.compteRenduObligatoire ? " dd-label-required" : ""}`}
                    >
                      Compte rendu
                    </label>
                    <textarea
                      className="dd-textarea"
                      value={compteRendu}
                      onChange={(e) => setCompteRendu(e.target.value)}
                      rows={3}
                      disabled={isFormDisabled}
                      required={champsRequis.compteRenduObligatoire}
                      placeholder={
                        champsRequis.compteRenduObligatoire
                          ? "Décrivez l'action effectuée…"
                          : "Optionnel pour cette transition"
                      }
                    />
                  </div>

                  {/* Code résultat */}
                  <div className="dd-field-wrap">
                    <label className="dd-label dd-label-required">
                      Code résultat
                    </label>
                    <select
                      className="dd-select"
                      value={codeResultat}
                      onChange={(e) => setCodeResultat(e.target.value)}
                      disabled={isFormDisabled}
                      required
                    >
                      <option value="">— Sélectionner —</option>
                      <option value="RC">RC — Right Contact</option>
                      <option value="RPC">RPC — Right Party Contact</option>
                      <option value="NRP">NRP — No Right Party</option>
                      <option value="NRC">NRC — No Reply Contact</option>
                      <option value="NUM_INVALIDE">
                        NUM_INVALIDE — Numéro invalide / hors service
                      </option>
                      <option value="REFUS">REFUS — Client refuse de payer</option>
                      <option value="DIFFERE">
                        DIFFERE — Client demande un délai
                      </option>
                      <option value="REGLE">
                        REGLE — Client confirme paiement effectué
                      </option>
                    </select>
                  </div>

                  {/* Récidive */}
                  {champsRequis.recidiveVisible && (
                    <div className="dd-field-wrap">
                      <label
                        className={`dd-label${champsRequis.recidiveObligatoire ? " dd-label-required" : ""}`}
                      >
                        Récidive
                      </label>
                      <div className="dd-toggle-group">
                        <button
                          type="button"
                          className={`dd-toggle-btn${!recidive ? " dd-toggle-active-no" : ""}`}
                          onClick={() => setRecidive(false)}
                          disabled={isFormDisabled}
                        >
                          Non
                        </button>
                        <button
                          type="button"
                          className={`dd-toggle-btn${recidive ? " dd-toggle-active-yes" : ""}`}
                          onClick={() => setRecidive(true)}
                          disabled={isFormDisabled}
                        >
                          Oui
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Montant promesse */}
                  {champsRequis.montantPromesseVisible && (
                    <div className="dd-field-wrap">
                      <label
                        className={`dd-label${champsRequis.montantPromesseObligatoire ? " dd-label-required" : ""}`}
                      >
                        Montant de la promesse (MAD)
                      </label>
                      <input
                        className="dd-input"
                        type="number"
                        value={montantPromesse}
                        onChange={(e) => setMontantPromesse(e.target.value)}
                        disabled={isFormDisabled}
                        required={champsRequis.montantPromesseObligatoire}
                        placeholder="Ex : 3 520"
                        min={0}
                      />
                    </div>
                  )}

                  {/* Date promesse */}
                  {champsRequis.datePromesseVisible && (
                    <div className="dd-field-wrap">
                      <label
                        className={`dd-label${champsRequis.datePromesseObligatoire ? " dd-label-required" : ""}`}
                      >
                        Date de la promesse
                      </label>
                      <input
                        type="date"
                        className="dd-input"
                        value={datePromesse}
                        onChange={(e) => setDatePromesse(e.target.value)}
                        disabled={isFormDisabled}
                        required={champsRequis.datePromesseObligatoire}
                        min={new Date().toISOString().split('T')[0]}
                      />
                    </div>
                  )}

                  {/* Nouveau téléphone */}
                  {champsRequis.nouveauTelephoneVisible && (
                    <div className="dd-field-wrap">
                      <label className="dd-label">
                        Nouveau téléphone qualifié
                      </label>
                      <input
                        className="dd-input dd-input-tel"
                        value={nouveauTelephone}
                        onChange={(e) => {
                          const v = e.target.value.replace(/[^0-9]/g, "");
                          if (v.length <= 10) setNouveauTelephone(v);
                        }}
                        disabled={isFormDisabled}
                        placeholder="0612345678"
                        maxLength={10}
                        pattern="0[67][0-9]{8}"
                      />
                      {nouveauTelephone && (
                        <p
                          className={`dd-hint ${nouveauTelephone.match(/^0[67][0-9]{8}$/) ? "dd-hint-success" : "dd-hint-error"}`}
                        >
                          {nouveauTelephone.match(/^0[67][0-9]{8}$/)
                            ? "✓ Format valide"
                            : "⚠ Format invalide — 10 chiffres commençant par 06 ou 07"}
                        </p>
                      )}
                    </div>
                  )}

                  {/* Adresse de cadrage pour PROMESSE et SUIVI_PROMESSE */}
                  {(nouveauStatut === "PROMESSE" ||
                    nouveauStatut === "SUIVI_PROMESSE") && (
                    <div className="dd-field-wrap">
                      <label className="dd-label">Adresse de cadrage</label>
                      <textarea
                        className="dd-textarea"
                        value={nouvelleAdresseCadrage}
                        onChange={(e) =>
                          setNouvelleAdresseCadrage(e.target.value)
                        }
                        rows={2}
                        disabled={isFormDisabled}
                        placeholder="Adresse exacte pour cadrage terrain..."
                      />
                      <p className="dd-hint">
                        Cette adresse sera utilisée pour les actions de cadrage
                        terrain
                      </p>
                    </div>
                  )}

                  {/* Bouton enregistrer */}
                  <button
                    className="dd-btn-save"
                    onClick={handleSave}
                    disabled={saving || isFormDisabled || !nouveauStatut}
                  >
                    {saving ? (
                      <>
                        <IconLoader /> Enregistrement…
                      </>
                    ) : (
                      <>
                        <IconSave /> Enregistrer l'action
                      </>
                    )}
                  </button>
                </>
              )}
            </div>
          )}

          {!canEdit && (
            <div className="dd-readonly-msg">
              <IconInfo />
              <p>Consultez l'historique des actions dans l'onglet approprié.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/* ─── Field Component ─────────────────────────────────────── */

function Field({
  label,
  value,
  textarea = false,
  highlight = false,
  icon,
}: {
  label: string | undefined;
  value: string | number | null | undefined;
  textarea?: boolean;
  highlight?: boolean;
  icon?: string;
}) {
  const displayValue =
    value === null || value === undefined || value === ""
      ? "—"
      : String(value);

  return (
    <div className="dd-field">
      <span className="dd-field-label">{label}</span>
      <div
        className={`dd-field-value${highlight ? " dd-field-highlight" : ""}`}
      >
        {icon && <span className="dd-field-icon">{icon}</span>}
        {textarea ? (
          <span className="dd-field-text dd-field-multiline">
            {displayValue}
          </span>
        ) : (
          <span className="dd-field-text">{displayValue}</span>
        )}
      </div>
    </div>
  );
}
