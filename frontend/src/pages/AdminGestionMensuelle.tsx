import { useState, useEffect } from "react";
import axios from "axios";
import {
  CalendarDays,
  Archive,
  Trash2,
  Upload,
  Download,
  Loader2,
  CheckCircle2,
  AlertCircle,
  History,
  FolderOpen,
} from "lucide-react";
import { telechargerCsvArchive } from "../services/importService";
import "./AdminGestionMensuelle.css";

const API = `${import.meta.env.VITE_API_URL}/api/archivage`;

type Message = { type: "success" | "error"; text: string };

const AdminGestionMensuelle = () => {
  const [moisActuel, setMoisActuel] = useState("");
  const [moisArchives, setMoisArchives] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<Message | null>(null);

  useEffect(() => {
    const now = new Date();
    const mois = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    setMoisActuel(mois);
    chargerMoisArchives();
  }, []);

  const chargerMoisArchives = async () => {
    try {
      const response = await axios.get(`${API}/mois`);
      setMoisArchives(response.data);
    } catch (error) {
      console.error("Erreur chargement mois archivés:", error);
    }
  };

  const archiverMois = async () => {
    if (
      !confirm(
        `Voulez-vous archiver le mois ${moisActuel} ?\n\nCette action va :\n- Exporter tous les dossiers en Excel\n- Copier les dossiers dans l'archive\n- Préparer la détection pour le mois suivant`,
      )
    ) {
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const response = await axios.post(`${API}/archiver`, {
        mois: moisActuel,
        exporterCsv: true,
      });

      if (response.data.success) {
        setMessage({
          type: "success",
          text: `${response.data.nbDossiersArchives} dossiers archivés avec succès${
            response.data.exportFileName ? ` — export : ${response.data.exportFileName}` : ""
          }`,
        });
        chargerMoisArchives();

        if (response.data.exportFileName) {
          setTimeout(async () => {
            try {
              await telechargerCsvArchive(moisActuel);
            } catch (error) {
              console.error("Erreur téléchargement automatique:", error);
            }
          }, 1500);
        }
      } else {
        setMessage({ type: "error", text: response.data.message });
      }
    } catch (error: any) {
      setMessage({
        type: "error",
        text: error.response?.data?.message || "Erreur lors de l'archivage",
      });
    } finally {
      setLoading(false);
    }
  };

  const nettoyerMois = async () => {
    if (
      !confirm(
        `ATTENTION : suppression définitive !\n\nVoulez-vous supprimer TOUS les dossiers du mois ${moisActuel} ?\n\nAssurez-vous que l'archivage a été effectué.\n\nCette action est IRRÉVERSIBLE.`,
      )
    ) {
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const response = await axios.delete(`${API}/nettoyer/${moisActuel}`);

      if (response.data.success) {
        setMessage({ type: "success", text: `${response.data.nbSupprimes} dossiers supprimés` });
      } else {
        setMessage({ type: "error", text: response.data.message });
      }
    } catch (error: any) {
      setMessage({
        type: "error",
        text: error.response?.data?.message || "Erreur lors du nettoyage",
      });
    } finally {
      setLoading(false);
    }
  };

  const importerNouveauMois = () => {
    window.location.href = "/admin/import";
  };

  const telechargerCsv = async (mois: string) => {
    try {
      await telechargerCsvArchive(mois);
      setMessage({ type: "success", text: `Téléchargement de l'export du mois ${mois} démarré` });
    } catch (error: any) {
      setMessage({
        type: "error",
        text: `Erreur lors du téléchargement : ${error.response?.data?.message || error.message}`,
      });
    }
  };

  return (
    <div className="gm-page">
      <header className="gm-header">
        <div>
          <h1 className="gm-title">Gestion Mensuelle</h1>
          <p className="gm-subtitle">Archivage, nettoyage et clôture des dossiers du mois</p>
        </div>
        <span className="gm-month-pill">
          <CalendarDays size={16} />
          Mois en cours : {moisActuel}
        </span>
      </header>

      {message && (
        <div className={`gm-alert gm-alert-${message.type}`}>
          {message.type === "success" ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{message.text}</span>
        </div>
      )}

      <section className="gm-card">
        <div className="gm-card-header">
          <span className="gm-card-icon is-brand">
            <CalendarDays size={18} />
          </span>
          <div>
            <h2>Clôture du mois {moisActuel}</h2>
            <p>Trois étapes pour passer au mois suivant</p>
          </div>
        </div>
        <div className="gm-card-body">
          <div className="gm-actions">
            <button
              className="gm-btn gm-btn-primary gm-btn-block"
              onClick={archiverMois}
              disabled={loading}
            >
              {loading ? (
                <Loader2 size={18} className="gm-spin" />
              ) : (
                <Archive size={18} />
              )}
              {loading ? "Archivage en cours…" : "Archiver et clôturer"}
            </button>

            <button
              className="gm-btn gm-btn-danger gm-btn-block"
              onClick={nettoyerMois}
              disabled={loading}
            >
              <Trash2 size={18} />
              Nettoyer le mois
            </button>

            <button
              className="gm-btn gm-btn-success gm-btn-block"
              onClick={importerNouveauMois}
              disabled={loading}
            >
              <Upload size={18} />
              Importer le mois suivant
            </button>
          </div>

          <div className="gm-steps">
            <h3>Processus mensuel recommandé</h3>
            <ol>
              <li>
                <span className="gm-step-num">1</span>
                <span>
                  <strong>Archiver</strong> — génère l'export Excel et sauvegarde tous les dossiers
                  dans l'historique.
                </span>
              </li>
              <li>
                <span className="gm-step-num">2</span>
                <span>
                  <strong>Nettoyer</strong> — supprime les dossiers actifs du mois, une fois
                  l'archivage confirmé.
                </span>
              </li>
              <li>
                <span className="gm-step-num">3</span>
                <span>
                  <strong>Importer</strong> — charge le nouveau mois avec détection automatique des
                  récurrents.
                </span>
              </li>
            </ol>
          </div>
        </div>
      </section>

      <section className="gm-card">
        <div className="gm-card-header">
          <span className="gm-card-icon">
            <History size={18} />
          </span>
          <div>
            <h2>Historique des mois archivés</h2>
            <p>
              {moisArchives.length} mois archivé{moisArchives.length > 1 ? "s" : ""} disponible
              {moisArchives.length > 1 ? "s" : ""}
            </p>
          </div>
        </div>
        <div className="gm-card-body">
          {moisArchives.length === 0 ? (
            <div className="gm-empty">
              <FolderOpen size={40} />
              <p>Aucun mois archivé pour le moment</p>
              <small>Les exports apparaîtront ici après la première clôture.</small>
            </div>
          ) : (
            <div className="gm-archive-grid">
              {moisArchives.map((mois) => (
                <article key={mois} className="gm-archive-card">
                  <div className="gm-archive-head">
                    <CalendarDays size={18} />
                    <span>{mois}</span>
                  </div>
                  <button
                    className="gm-btn gm-btn-ghost gm-btn-sm"
                    onClick={() => telechargerCsv(mois)}
                  >
                    <Download size={16} />
                    Télécharger Excel
                  </button>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
};

export default AdminGestionMensuelle;
