import { useState, useRef, useCallback } from "react";
import { importExcel, importExcelAvecDetection, resetDatabase } from "../services/importService";
import type { ImportRapport, ImportAvecDetectionRapport } from "../services/importService";
import "./AdminImport.css";

/* ─── Icons ───────────────────────────────────────────────── */
const IconUpload = () => (
  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
    <polyline points="17 8 12 3 7 8"/>
    <line x1="12" y1="3" x2="12" y2="15"/>
  </svg>
);
const IconFile = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
    <polyline points="14 2 14 8 20 8"/>
    <line x1="16" y1="13" x2="8" y2="13"/>
    <line x1="16" y1="17" x2="8" y2="17"/>
    <polyline points="10 9 9 9 8 9"/>
  </svg>
);
const IconTrash = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
    <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
  </svg>
);
const IconCheck = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"/>
  </svg>
);
const IconX = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);
const IconRefresh = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="23 4 23 10 17 10"/>
    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
  </svg>
);
const IconLoader = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="ai-spin">
    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
  </svg>
);
const IconInfo = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
  </svg>
);
const IconWarning = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
    <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
  </svg>
);
const IconPlay = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
    <polygon points="5 3 19 12 5 21 5 3"/>
  </svg>
);

/* ─── Confirm Modal ───────────────────────────────────────── */
function ConfirmModal({ onConfirm, onCancel }: { onConfirm: () => void; onCancel: () => void }) {
  const [typed, setTyped] = useState("");
  const valid = typed === "SUPPRIMER";

  return (
    <div className="ai-modal-overlay" onClick={onCancel}>
      <div className="ai-modal" onClick={e => e.stopPropagation()}>
        <div className="ai-modal-icon">
          <IconWarning />
        </div>
        <h3 className="ai-modal-title">Réinitialiser la base de données ?</h3>
        <p className="ai-modal-body">
          Cette action est <strong>irréversible</strong>. Elle supprimera <strong>tous</strong> les dossiers,
          crédits, clients et agents. L'administrateur sera conservé.
        </p>
        <div className="ai-modal-confirm-field">
          <label>Tapez <strong>SUPPRIMER</strong> pour confirmer</label>
          <input
            className="ai-modal-input"
            type="text"
            value={typed}
            onChange={e => setTyped(e.target.value)}
            placeholder="SUPPRIMER"
            autoFocus
          />
        </div>
        <div className="ai-modal-actions">
          <button className="ai-btn-cancel" onClick={onCancel}>Annuler</button>
          <button className="ai-btn-danger" disabled={!valid} onClick={onConfirm}>
            <IconTrash /> Supprimer définitivement
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─── Reset Result Panel ──────────────────────────────────── */
function ResetResult({ result, onClose }: { result: any; onClose: () => void }) {
  return (
    <div className="ai-reset-result">
      <div className="ai-reset-result-icon"><IconCheck /></div>
      <div className="ai-reset-result-body">
        <p className="ai-reset-result-title">Base réinitialisée avec succès</p>
        <p className="ai-reset-result-sub">
          {result.suppressed.actions} actions · {result.suppressed.dossiers} dossiers ·{" "}
          {result.suppressed.credits} crédits · {result.suppressed.clients} clients ·{" "}
          {result.suppressed.agents} agents supprimés
        </p>
      </div>
      <button className="ai-reset-result-close" onClick={onClose}><IconX /></button>
    </div>
  );
}

/* ─── Main Component ──────────────────────────────────────── */
export default function AdminImport() {
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);
  const [rapport, setRapport] = useState<ImportRapport | null>(null);
  const [rapportDetection, setRapportDetection] = useState<ImportAvecDetectionRapport | null>(null);
  const [loading, setLoading] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [resetResult, setResetResult] = useState<any>(null);
  const [importError, setImportError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Date d'import (date complète au lieu de mois seulement)
  const now = new Date();
  const dateAujourdhui = now.toISOString().split('T')[0]; // Format YYYY-MM-DD
  const [dateImport, setDateImport] = useState<string>(dateAujourdhui);
  const [useDetection, setUseDetection] = useState<boolean>(true); // Utiliser la détection des récurrents

  /* ── Drag & drop ── */
  const onDragOver = useCallback((e: React.DragEvent) => { e.preventDefault(); setDragging(true); }, []);
  const onDragLeave = useCallback(() => setDragging(false), []);
  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped && /\.(xlsx|xls)$/.test(dropped.name)) {
      setFile(dropped);
      setRapport(null);
      setImportError(null);
    }
  }, []);

  const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0] ?? null;
    setFile(f);
    setRapport(null);
    setImportError(null);
  };

  /* ── Import ── */
  const handleImport = async () => {
    if (!file) return;
    setLoading(true);
    setRapport(null);
    setRapportDetection(null);
    setImportError(null);
    try {
      if (useDetection) {
        // Import avec détection des récurrents
        const result = await importExcelAvecDetection(file, dateImport);
        setRapportDetection(result);
      } else {
        // Import standard (sans détection)
        const result = await importExcel(file, dateImport);
        setRapport(result);
      }
    } catch (e: any) {
      setImportError(e?.response?.data?.message ?? "Erreur lors de l'import. Vérifiez le format du fichier.");
    } finally {
      setLoading(false);
    }
  };

  /* ── Reset ── */
  const handleReset = async () => {
    setShowConfirm(false);
    setResetLoading(true);
    try {
      const result = await resetDatabase();
      setResetResult(result);
      setRapport(null);
    } catch (e: any) {
      setImportError("Erreur lors de la réinitialisation : " + (e.message ?? "inconnue"));
    } finally {
      setResetLoading(false);
    }
  };

  const successRate = rapport
    ? Math.round((rapport.nbInserees / Math.max(rapport.nbLignesTotal, 1)) * 100)
    : 0;

  return (
    <div className="ai-page">
      {showConfirm && <ConfirmModal onConfirm={handleReset} onCancel={() => setShowConfirm(false)} />}

      {/* ── Page header ─ */}
      <div className="page-header">
        <div>
          <h2 className="page-title">Import Excel</h2>
          <p className="page-subtitle">Chargez votre fichier de dossiers pour mettre à jour la base</p>
        </div>
      </div>

      <div className="ai-layout">

        {/* ── LEFT col ──────────────────────────────────────── */}
        <div className="ai-left">

          {/* ── Sélecteur de date d'import ── */}
          <div className="ai-month-selector">
            <div className="ai-month-header">
              <span className="ai-month-icon">📅</span>
              <div>
                <h3 className="ai-month-title">Date d'import</h3>
                <p className="ai-month-sub">Définissez la date exacte à laquelle appartiennent ces dossiers</p>
              </div>
            </div>
            <div className="ai-month-inputs">
              <div className="ai-input-group">
                <label>Date d'import</label>
                <input
                  type="date"
                  value={dateImport}
                  onChange={e => setDateImport(e.target.value)}
                  className="ai-month-input"
                  max={dateAujourdhui}
                />
              </div>
              <div className="ai-input-group">
                <label>Mode d'import</label>
                <div className="ai-toggle-group">
                  <button
                    className={`ai-toggle-btn ${useDetection ? 'active' : ''}`}
                    onClick={() => setUseDetection(true)}
                    title="Détecte automatiquement les dossiers qui existaient le mois précédent et leur assigne le bon statut"
                  >
                    🔄 Avec détection récurrents
                  </button>
                  <button
                    className={`ai-toggle-btn ${!useDetection ? 'active' : ''}`}
                    onClick={() => setUseDetection(false)}
                  >
                    📥 Import standard
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Drop zone */}
          <div
            className={`ai-dropzone${dragging ? " ai-dropzone-over" : ""}${file ? " ai-dropzone-has-file" : ""}`}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
            onClick={() => !file && fileInputRef.current?.click()}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".xlsx,.xls"
              onChange={onFileChange}
              style={{ display: "none" }}
            />

            {!file ? (
              <>
                <div className="ai-dropzone-icon"><IconUpload /></div>
                <p className="ai-dropzone-title">Glissez votre fichier ici</p>
                <p className="ai-dropzone-sub">ou <span className="ai-link">parcourir</span> depuis votre ordinateur</p>
                <p className="ai-dropzone-formats">.xlsx · .xls</p>
              </>
            ) : (
              <div className="ai-file-info">
                <div className="ai-file-icon"><IconFile /></div>
                <div className="ai-file-details">
                  <span className="ai-file-name">{file.name}</span>
                  <span className="ai-file-size">{(file.size / 1024).toFixed(1)} Ko</span>
                </div>
                <button
                  className="ai-file-remove"
                  onClick={e => { e.stopPropagation(); setFile(null); setRapport(null); setImportError(null); }}
                  title="Supprimer"
                >
                  <IconX />
                </button>
              </div>
            )}
          </div>

          {/* Import error */}
          {importError && (
            <div className="ai-error-banner">
              <IconX />
              <span>{importError}</span>
            </div>
          )}

          {/* Reset result */}
          {resetResult && (
            <ResetResult result={resetResult} onClose={() => setResetResult(null)} />
          )}

          {/* Launch button */}
          <button
            className="ai-btn-primary"
            onClick={handleImport}
            disabled={!file || loading}
          >
            {loading ? <><IconLoader /> Import en cours…</> : <><IconPlay /> Lancer l'import</>}
          </button>

          {/* Progress bar while loading */}
          {loading && (
            <div className="ai-progress-wrap">
              <div className="ai-progress-bar" />
            </div>
          )}

          {/* Colonnes hint */}
          <div className="ai-hint-card">
            <div className="ai-hint-header">
              <IconInfo />
              <span>Colonnes requises dans le fichier Excel</span>
            </div>
            <div className="ai-columns-grid">
              {["num_cin1","nom","prenom","cli_tel","TEL2","cli_adr","Lib_Ville","cod_age","lib_age",
                "Adresse_Cadrage","ANC_REFDO","From_Prov","GEST_CIN","CSP_PDT","Segment","nbr_imp",
                "Mensualite","encours","Mnt_Imp","prv_cum","stage","DVR","Reecheance","Immatriculation"
              ].map(col => (
                <span key={col} className="ai-col-tag">{col}</span>
              ))}
            </div>
            <p className="ai-hint-note">
              📌 Si une colonne <code>Agent_Email</code> est présente, les dossiers seront assignés à l'agent correspondant (créé automatiquement si inexistant).
            </p>
          </div>
          
          {/* Redirection vers Gestion Mensuelle pour l'archivage */}
          <div className="ai-info-card">
            <div className="ai-info-header">
              <IconInfo />
              <h3>Archivage mensuel</h3>
            </div>
            <p className="ai-info-text">
              L'archivage des dossiers se fait depuis la page <strong>Gestion Mensuelle</strong>.
              <br/>
              Cette page offre un processus complet : archivage, export Excel et clôture du mois.
            </p>
            <button
              className="ai-btn-secondary"
              onClick={() => window.location.href = '/admin/gestion-mensuelle'}
            >
              📅 Accéder à la Gestion Mensuelle
            </button>
          </div>
        </div>

        {/* ── RIGHT col ─────────────────────────────────────── */}
        <div className="ai-right">

          {/* Rapport import avec détection */}
          {rapportDetection && (
            <div className="ai-rapport">
              <div className="ai-rapport-header">
                <div>
                  <h3 className="ai-rapport-title">Rapport d'import avec détection</h3>
                  <span className="ai-rapport-job">Mois {rapportDetection.moisImporte}</span>
                </div>
                <div className={`ai-rapport-badge ${rapportDetection.success ? 'badge-success' : 'badge-error'}`}>
                  {rapportDetection.success ? '✅ Réussi' : '❌ Erreur'}
                </div>
              </div>

              {/* Stats cards détection */}
              <div className="ai-stats">
                <div className="ai-stat ai-stat-neutral">
                  <span className="ai-stat-value">{rapportDetection.totalDossiers}</span>
                  <span className="ai-stat-label">Total importés</span>
                </div>
                <div className="ai-stat ai-stat-success">
                  <span className="ai-stat-value">{rapportDetection.nouveauxDossiers}</span>
                  <span className="ai-stat-label">🆕 Nouveaux</span>
                </div>
                <div className="ai-stat ai-stat-info">
                  <span className="ai-stat-value">{rapportDetection.dossiersRecurrents}</span>
                  <span className="ai-stat-label">🔄 Récurrents</span>
                </div>
              </div>

              {/* Répartition des statuts */}
              {Object.keys(rapportDetection.repartitionStatuts).length > 0 && (
                <div className="ai-repartition">
                  <h4 className="ai-repartition-title">📊 Répartition des statuts assignés</h4>
                  <div className="ai-repartition-grid">
                    {Object.entries(rapportDetection.repartitionStatuts).map(([statut, nb]) => (
                      <div key={statut} className="ai-repartition-item">
                        <span className="ai-repartition-statut">{statut}</span>
                        <span className="ai-repartition-count">{nb}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Répartition récurrents */}
              {rapportDetection.dossiersRecurrents > 0 && Object.keys(rapportDetection.repartitionRecurrents).length > 0 && (
                <div className="ai-repartition">
                  <h4 className="ai-repartition-title">🔄 Statuts des récurrents (transition auto)</h4>
                  <div className="ai-repartition-grid">
                    {Object.entries(rapportDetection.repartitionRecurrents).map(([statut, nb]) => (
                      <div key={statut} className="ai-repartition-item ai-repartition-recurrent">
                        <span className="ai-repartition-statut">{statut}</span>
                        <span className="ai-repartition-count">{nb}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Rapport import standard */}
          {rapport && (
            <div className="ai-rapport">
              <div className="ai-rapport-header">
                <div>
                  <h3 className="ai-rapport-title">Rapport d'import</h3>
                  <span className="ai-rapport-job">Job #{rapport.jobId}</span>
                </div>
                <div
                  className={`ai-rapport-badge ${successRate === 100 ? "badge-success" : successRate >= 80 ? "badge-warn" : "badge-error"}`}
                >
                  {successRate}% réussi
                </div>
              </div>

              {/* Stats cards */}
              <div className="ai-stats">
                <div className="ai-stat ai-stat-neutral">
                  <span className="ai-stat-value">{rapport.nbLignesTotal}</span>
                  <span className="ai-stat-label">Lignes traitées</span>
                </div>
                <div className="ai-stat ai-stat-success">
                  <span className="ai-stat-value">{rapport.nbInserees}</span>
                  <span className="ai-stat-label">Insérées</span>
                </div>
                <div className="ai-stat ai-stat-info">
                  <span className="ai-stat-value">{rapport.nbMisesAJour}</span>
                  <span className="ai-stat-label">Mises à jour</span>
                </div>
                <div className="ai-stat ai-stat-error">
                  <span className="ai-stat-value">{rapport.nbRejetees}</span>
                  <span className="ai-stat-label">Rejetées</span>
                </div>
              </div>

              {/* Progress ring visual */}
              <div className="ai-progress-row">
                <div className="ai-bar-item">
                  <div className="ai-bar-label"><span>Insérées</span><span className="ai-bar-pct success">{rapport.nbInserees}</span></div>
                  <div className="ai-bar-track"><div className="ai-bar-fill ai-bar-success" style={{ width: `${(rapport.nbInserees / Math.max(rapport.nbLignesTotal, 1)) * 100}%` }} /></div>
                </div>
                <div className="ai-bar-item">
                  <div className="ai-bar-label"><span>Mises à jour</span><span className="ai-bar-pct info">{rapport.nbMisesAJour}</span></div>
                  <div className="ai-bar-track"><div className="ai-bar-fill ai-bar-info" style={{ width: `${(rapport.nbMisesAJour / Math.max(rapport.nbLignesTotal, 1)) * 100}%` }} /></div>
                </div>
                <div className="ai-bar-item">
                  <div className="ai-bar-label"><span>Rejetées</span><span className="ai-bar-pct error">{rapport.nbRejetees}</span></div>
                  <div className="ai-bar-track"><div className="ai-bar-fill ai-bar-error" style={{ width: `${(rapport.nbRejetees / Math.max(rapport.nbLignesTotal, 1)) * 100}%` }} /></div>
                </div>
              </div>

              {/* Erreurs */}
              {rapport.erreurs.length > 0 && (
                <div className="ai-erreurs">
                  <h4 className="ai-erreurs-title">
                    <IconX /> {rapport.erreurs.length} erreur{rapport.erreurs.length > 1 ? "s" : ""} détectée{rapport.erreurs.length > 1 ? "s" : ""}
                  </h4>
                  <div className="ai-erreurs-list">
                    {rapport.erreurs.map((e, i) => (
                      <div key={i} className="ai-erreur-row">
                        <span className="ai-erreur-ligne">L.{e.ligne}</span>
                        <span className="ai-erreur-ref">{e.referenceContrat || "—"}</span>
                        <span className="ai-erreur-motif">{e.motif}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Zone dangereuse */}
          <div className="ai-danger-zone">
            <div className="ai-danger-header">
              <div className="ai-danger-icon"><IconWarning /></div>
              <div>
                <h3 className="ai-danger-title">Zone dangereuse</h3>
                <p className="ai-danger-sub">Action irréversible — à utiliser avec précaution</p>
              </div>
            </div>
            <p className="ai-danger-desc">
              Réinitialiser la base supprime <strong>tous</strong> les dossiers, crédits, clients et agents.
              L'administrateur est conservé. Utilisez cette option pour repartir de zéro avant un nouvel import.
            </p>
            <button
              className="ai-btn-danger"
              onClick={() => setShowConfirm(true)}
              disabled={resetLoading}
            >
              {resetLoading ? <><IconLoader /> Suppression…</> : <><IconRefresh /> Réinitialiser la base</>}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}