-- ================================================================
-- SCRIPT DE CRÉATION DU SYSTÈME D'ARCHIVAGE MENSUEL
-- ================================================================
-- Description : Création des tables pour gérer l'archivage mensuel
--               et la détection automatique des dossiers récurrents
-- ================================================================

-- 1. TABLE DOSSIERS_ARCHIVE
-- Archive complète de tous les dossiers traités
CREATE TABLE IF NOT EXISTS dossiers_archive (
    id BIGSERIAL PRIMARY KEY,
    
    -- Données du dossier (copie de la table dossiers)
    numero_dossier VARCHAR(50) NOT NULL,
    nom_client VARCHAR(255),
    telephone VARCHAR(20),
    email VARCHAR(255),
    montant_pret DECIMAL(15, 2),
    montant_echeance DECIMAL(15, 2),
    nombre_echeances_impayees INTEGER,
    total_du DECIMAL(15, 2),
    date_premier_impaye DATE,
    anciennete_impaye INTEGER,
    statut VARCHAR(50),
    agent_id BIGINT,
    date_promesse DATE,
    montant_promesse DECIMAL(15, 2),
    commentaire TEXT,
    date_derniere_action TIMESTAMP,
    score_priorite INTEGER,
    mois_import VARCHAR(7),
    
    -- Métadonnées d'archivage
    mois_archive VARCHAR(7) NOT NULL,  -- Format: "2024-07"
    date_archivage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_final VARCHAR(50),          -- Dernier statut avant archivage
    
    -- Index pour performance
    CONSTRAINT idx_archive_unique UNIQUE (numero_dossier, mois_archive)
);

-- Index pour recherches rapides
CREATE INDEX IF NOT EXISTS idx_archive_mois ON dossiers_archive(mois_archive);
CREATE INDEX IF NOT EXISTS idx_archive_numero ON dossiers_archive(numero_dossier);
CREATE INDEX IF NOT EXISTS idx_archive_date ON dossiers_archive(date_archivage);
CREATE INDEX IF NOT EXISTS idx_archive_statut ON dossiers_archive(statut_final);

-- 2. TABLE DOSSIERS_MOIS_PRECEDENT
-- Table temporaire pour stocker les infos du mois précédent
CREATE TABLE IF NOT EXISTS dossiers_mois_precedent (
    numero_dossier VARCHAR(50) PRIMARY KEY,
    statut_final VARCHAR(50) NOT NULL,
    date_derniere_action TIMESTAMP,
    mois_origine VARCHAR(7) NOT NULL,
    date_sauvegarde TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_precedent_mois ON dossiers_mois_precedent(mois_origine);

-- 3. AJOUT COLONNE EST_RECURRENT DANS DOSSIERS
ALTER TABLE dossiers 
ADD COLUMN IF NOT EXISTS est_recurrent BOOLEAN DEFAULT FALSE;

ALTER TABLE dossiers 
ADD COLUMN IF NOT EXISTS mois_precedent_statut VARCHAR(50);

-- Index sur la colonne
CREATE INDEX IF NOT EXISTS idx_dossiers_recurrent ON dossiers(est_recurrent);

-- 4. TABLE LOGS_ARCHIVAGE
-- Pour tracer toutes les opérations d'archivage
CREATE TABLE IF NOT EXISTS logs_archivage (
    id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL, -- 'EXPORT', 'ARCHIVAGE', 'NETTOYAGE', 'IMPORT'
    mois_concerne VARCHAR(7) NOT NULL,
    date_operation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nb_dossiers INTEGER,
    nb_recurrents INTEGER,
    nb_nouveaux INTEGER,
    utilisateur_id BIGINT,
    details TEXT,
    statut VARCHAR(20) DEFAULT 'SUCCESS', -- 'SUCCESS', 'ERROR', 'PARTIAL'
    
    FOREIGN KEY (utilisateur_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_logs_mois ON logs_archivage(mois_concerne);
CREATE INDEX IF NOT EXISTS idx_logs_date ON logs_archivage(date_operation);

-- 5. VUE STATISTIQUES ARCHIVAGE
-- Vue pour faciliter les rapports
CREATE OR REPLACE VIEW v_stats_archivage AS
SELECT 
    mois_archive,
    COUNT(*) as total_dossiers,
    COUNT(CASE WHEN statut_final = 'CONTACTE' THEN 1 END) as contactes,
    COUNT(CASE WHEN statut_final = 'PROMESSE' THEN 1 END) as promesses,
    COUNT(CASE WHEN statut_final = 'INJOIGNABLE' THEN 1 END) as injoignables,
    COUNT(CASE WHEN statut_final = 'EN_ATTENTE' THEN 1 END) as en_attente,
    SUM(total_du) as total_du_mensuel,
    MIN(date_archivage) as date_archivage
FROM dossiers_archive
GROUP BY mois_archive
ORDER BY mois_archive DESC;

-- 6. FONCTION POUR CALCULER LE STATUT AUTO DES RÉCURRENTS
-- Retourne VARCHAR au lieu de statut_dossier pour éviter les problèmes de cast
CREATE OR REPLACE FUNCTION calculer_statut_recurrent(ancien_statut VARCHAR)
RETURNS VARCHAR AS $$
BEGIN
    RETURN CASE 
        WHEN ancien_statut = 'CONTACTE' THEN 'RELANCE'
        WHEN ancien_statut = 'PROMESSE' THEN 'SUIVI_PROMESSE'
        WHEN ancien_statut = 'INJOIGNABLE' THEN 'INJOIGNABLE'
        WHEN ancien_statut = 'EN_ATTENTE' THEN 'EN_ATTENTE'
        WHEN ancien_statut = 'RELANCE' THEN 'RELANCE'
        WHEN ancien_statut = 'SUIVI_PROMESSE' THEN 'SUIVI_PROMESSE'
        ELSE 'EN_ATTENTE'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- 7. FONCTION POUR ARCHIVER UN MOIS
CREATE OR REPLACE FUNCTION archiver_mois(p_mois VARCHAR)
RETURNS TABLE(nb_archives BIGINT, statut TEXT) AS $$
DECLARE
    v_count BIGINT;
BEGIN
    -- Copier tous les dossiers vers l'archive
    INSERT INTO dossiers_archive (
        numero_dossier, nom_client, telephone, email, 
        montant_pret, montant_echeance, nombre_echeances_impayees,
        total_du, date_premier_impaye, anciennete_impaye,
        statut, agent_id, date_promesse, montant_promesse,
        commentaire, date_derniere_action, score_priorite, mois_import,
        mois_archive, statut_final
    )
    SELECT 
        numero_dossier, nom_client, telephone, email,
        montant_pret, montant_echeance, nombre_echeances_impayees,
        total_du, date_premier_impaye, anciennete_impaye,
        statut, agent_id, date_promesse, montant_promesse,
        commentaire, date_derniere_action, score_priorite, mois_import,
        p_mois, statut
    FROM dossiers
    WHERE mois_import = p_mois;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    -- Sauvegarder dans table temporaire pour détection
    INSERT INTO dossiers_mois_precedent (numero_dossier, statut_final, date_derniere_action, mois_origine)
    SELECT numero_dossier, statut, date_derniere_action, p_mois
    FROM dossiers
    WHERE mois_import = p_mois
    ON CONFLICT (numero_dossier) DO UPDATE 
    SET statut_final = EXCLUDED.statut_final,
        date_derniere_action = EXCLUDED.date_derniere_action,
        mois_origine = EXCLUDED.mois_origine;
    
    RETURN QUERY SELECT v_count, 'SUCCESS';
END;
$$ LANGUAGE plpgsql;

-- 8. FONCTION POUR NETTOYER LES DOSSIERS DU MOIS
CREATE OR REPLACE FUNCTION nettoyer_dossiers(p_mois VARCHAR)
RETURNS TABLE(nb_supprimes BIGINT, statut TEXT) AS $$
DECLARE
    v_count BIGINT;
BEGIN
    -- Vérifier que l'archivage a été fait
    IF NOT EXISTS (SELECT 1 FROM dossiers_archive WHERE mois_archive = p_mois LIMIT 1) THEN
        RETURN QUERY SELECT 0::BIGINT, 'ERROR: Archivage non effectué';
        RETURN;
    END IF;
    
    -- Supprimer les dossiers
    DELETE FROM dossiers WHERE mois_import = p_mois;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    RETURN QUERY SELECT v_count, 'SUCCESS';
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- DONNÉES DE TEST
-- ================================================================

-- Vérification
SELECT 'Tables créées avec succès!' as message;
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');
