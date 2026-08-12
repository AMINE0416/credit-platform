-- ═══════════════════════════════════════════════════════════
-- Migration : Ajout du système d'historique des dossiers
-- Version : 1.0.0
-- Date : 2024
-- ═══════════════════════════════════════════════════════════

-- 1. Créer la table d'historique si elle n'existe pas
CREATE TABLE IF NOT EXISTS dossier_historique (
    id BIGSERIAL PRIMARY KEY,
    
    -- Période d'archivage
    mois INTEGER NOT NULL,
    annee INTEGER NOT NULL,
    date_archivage TIMESTAMP NOT NULL,
    
    -- Informations Dossier
    dossier_id_origine BIGINT,
    statut_dossier VARCHAR(50),
    agent_assigne_email VARCHAR(255),
    agent_assigne_nom VARCHAR(255),
    
    -- Informations Client
    cin VARCHAR(50),
    nom VARCHAR(100),
    prenom VARCHAR(100),
    telephone VARCHAR(50),
    telephone2 VARCHAR(50),
    adresse VARCHAR(255),
    ville VARCHAR(100),
    code_agence VARCHAR(50),
    lib_agence VARCHAR(100),
    adresse_cadrage VARCHAR(255),
    
    -- Informations Crédit
    reference_contrat VARCHAR(50),
    from_prov VARCHAR(50),
    gest_cin VARCHAR(50),
    type_credit VARCHAR(100),
    segment VARCHAR(50),
    nbr_impaye INTEGER,
    mensualite DECIMAL(15,2),
    encours DECIMAL(15,2),
    impaye_montant DECIMAL(15,2),
    prv_cum DECIMAL(15,2),
    stage VARCHAR(50),
    dvr VARCHAR(50),
    reecheance VARCHAR(50),
    immatriculation VARCHAR(50),
    score DECIMAL(5,2),
    
    -- Statistiques à l'archivage
    nb_actions_total INTEGER,
    date_derniere_action TIMESTAMP,
    
    -- Contrainte d'unicité pour éviter les doublons
    CONSTRAINT unique_dossier_mois_annee UNIQUE (dossier_id_origine, mois, annee)
);

-- 2. Créer les index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_historique_mois_annee 
    ON dossier_historique(mois, annee);

CREATE INDEX IF NOT EXISTS idx_historique_dossier_origine 
    ON dossier_historique(dossier_id_origine);

CREATE INDEX IF NOT EXISTS idx_historique_cin 
    ON dossier_historique(cin);

CREATE INDEX IF NOT EXISTS idx_historique_reference 
    ON dossier_historique(reference_contrat);

CREATE INDEX IF NOT EXISTS idx_historique_ville 
    ON dossier_historique(ville);

CREATE INDEX IF NOT EXISTS idx_historique_agent 
    ON dossier_historique(agent_assigne_email);

-- 3. Ajouter des commentaires pour la documentation
COMMENT ON TABLE dossier_historique IS 
    'Archive mensuelle des dossiers avant chaque import - permet de conserver un historique complet des états';

COMMENT ON COLUMN dossier_historique.mois IS 
    'Mois de l''archive (1-12)';

COMMENT ON COLUMN dossier_historique.annee IS 
    'Année de l''archive (ex: 2024)';

COMMENT ON COLUMN dossier_historique.dossier_id_origine IS 
    'ID du dossier original dans la table dossiers';

COMMENT ON COLUMN dossier_historique.date_archivage IS 
    'Date et heure de création de l''archive';

-- 4. Vérification de la migration
DO $$
DECLARE
    table_exists BOOLEAN;
    index_count INTEGER;
BEGIN
    -- Vérifier que la table a été créée
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'dossier_historique'
    ) INTO table_exists;
    
    IF table_exists THEN
        RAISE NOTICE '✅ Table dossier_historique créée avec succès';
    ELSE
        RAISE EXCEPTION '❌ Erreur : La table dossier_historique n''a pas été créée';
    END IF;
    
    -- Compter les index créés
    SELECT COUNT(*) 
    INTO index_count
    FROM pg_indexes 
    WHERE tablename = 'dossier_historique';
    
    RAISE NOTICE '✅ % index créés sur la table dossier_historique', index_count;
    
    -- Afficher le résumé
    RAISE NOTICE '═══════════════════════════════════════════════════════';
    RAISE NOTICE 'Migration terminée avec succès !';
    RAISE NOTICE 'Table : dossier_historique';
    RAISE NOTICE 'Index : %', index_count;
    RAISE NOTICE '═══════════════════════════════════════════════════════';
END $$;

-- 5. Exemple d'utilisation (commenté par défaut)
/*
-- Archiver les données actuelles pour le mois 7 (juillet 2024)
-- À exécuter depuis l'API : POST /api/historique/archiver?mois=7&annee=2024

-- Vérifier l'archivage
SELECT 
    mois,
    annee,
    COUNT(*) as nb_dossiers,
    COUNT(DISTINCT agent_assigne_email) as nb_agents,
    MIN(date_archivage) as date_archive,
    AVG(score) as score_moyen
FROM dossier_historique
GROUP BY mois, annee
ORDER BY annee DESC, mois DESC;

-- Consulter les dossiers archivés d'un mois
SELECT 
    cin,
    nom,
    prenom,
    reference_contrat,
    statut_dossier,
    agent_assigne_nom,
    score
FROM dossier_historique
WHERE mois = 7 AND annee = 2024
ORDER BY score DESC
LIMIT 10;
*/
