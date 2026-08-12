-- Table d'historique pour archiver les dossiers lors de chaque import mensuel
-- Permet de conserver un snapshot complet des données à chaque période (mois 7, 8, 9, etc.)

CREATE TABLE IF NOT EXISTS dossier_historique (
    id BIGSERIAL PRIMARY KEY,
    
    -- Période d'archivage
    mois INTEGER NOT NULL,                          -- Ex: 7 pour juillet, 8 pour août
    annee INTEGER NOT NULL,                         -- Ex: 2024
    date_archivage TIMESTAMP NOT NULL,              -- Date de création de l'archive
    
    -- Informations Dossier
    dossier_id_origine BIGINT,                      -- ID du dossier original
    statut_dossier VARCHAR(50),                     -- NOUVEAU, EN_COURS, PROMESSE, etc.
    agent_assigne_email VARCHAR(255),               -- Email de l'agent assigné
    agent_assigne_nom VARCHAR(255),                 -- Nom complet de l'agent
    
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
    nb_actions_total INTEGER,                       -- Nombre d'actions sur ce dossier
    date_derniere_action TIMESTAMP,                 -- Date de la dernière action
    
    -- Index pour performances
    CONSTRAINT unique_dossier_mois_annee UNIQUE (dossier_id_origine, mois, annee)
);

-- Index pour recherches fréquentes
CREATE INDEX idx_historique_mois_annee ON dossier_historique(mois, annee);
CREATE INDEX idx_historique_dossier_origine ON dossier_historique(dossier_id_origine);
CREATE INDEX idx_historique_cin ON dossier_historique(cin);
CREATE INDEX idx_historique_reference ON dossier_historique(reference_contrat);

-- Commentaires
COMMENT ON TABLE dossier_historique IS 'Archive mensuelle des dossiers avant chaque import';
COMMENT ON COLUMN dossier_historique.mois IS 'Mois de l archive (1-12)';
COMMENT ON COLUMN dossier_historique.annee IS 'Année de l archive';
COMMENT ON COLUMN dossier_historique.dossier_id_origine IS 'Référence vers le dossier original';
