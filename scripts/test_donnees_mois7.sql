-- ================================================================
-- SCRIPT DE TEST : CRÉATION DONNÉES MOIS 7 (Juillet 2024)
-- ================================================================
-- Description : Crée des données de test pour simuler un import
--               du mois de juillet 2024
-- ================================================================

-- Nettoyer les données de test précédentes si nécessaire
-- DELETE FROM dossiers WHERE mois_import LIKE '2024-%';
-- DELETE FROM credits WHERE reference_contrat LIKE 'DOSS-%';
-- DELETE FROM clients WHERE cin LIKE 'TEST_%';

-- 1. CRÉER LES CLIENTS DE TEST
-- Note: La colonne 'email' n'existe pas dans la table clients
INSERT INTO clients (cin, nom, prenom, telephone, telephone2, adresse, ville, code_agence, lib_agence) 
VALUES
('TEST_AB123456', 'BENALI', 'Mohammed', '0612345678', '0712345678', '12 Rue Hassan II', 'Casablanca', 'CAS001', 'Agence Casablanca Centre'),
('TEST_CD789012', 'ALAMI', 'Fatima', '0623456789', '0723456789', '34 Av Mohammed V', 'Rabat', 'RAB001', 'Agence Rabat Agdal'),
('TEST_EF345678', 'IDRISSI', 'Youssef', '0634567890', '0734567890', '56 Bd Zerktouni', 'Casablanca', 'CAS002', 'Agence Maarif'),
('TEST_GH901234', 'BOUAZZA', 'Samira', '0645678901', '0745678901', '78 Rue Tanger', 'Fès', 'FES001', 'Agence Fès Centre'),
('TEST_IJ567890', 'ZOUAOUI', 'Karim', '0656789012', '0756789012', '90 Av FAR', 'Marrakech', 'MAR001', 'Agence Marrakech Guéliz')
ON CONFLICT (cin) DO NOTHING;

-- 2. CRÉER LES CRÉDITS DE TEST
INSERT INTO credits (
    client_id, 
    reference_contrat, 
    type_credit, 
    segment, 
    nbr_impaye, 
    mensualite, 
    encours, 
    impaye_montant,
    prv_cum,
    stage,
    score
) 
SELECT 
    c.id,
    'DOSS-TEST-' || LPAD((ROW_NUMBER() OVER())::text, 3, '0'),
    'AUTO_CLASSIQUE',
    'AUTO PP',
    CASE 
        WHEN c.cin = 'TEST_AB123456' THEN 2
        WHEN c.cin = 'TEST_CD789012' THEN 1
        WHEN c.cin = 'TEST_EF345678' THEN 3
        WHEN c.cin = 'TEST_GH901234' THEN 2
        ELSE 1
    END,
    CASE 
        WHEN c.cin = 'TEST_AB123456' THEN 1500.00
        WHEN c.cin = 'TEST_CD789012' THEN 2000.00
        WHEN c.cin = 'TEST_EF345678' THEN 1200.00
        WHEN c.cin = 'TEST_GH901234' THEN 1800.00
        ELSE 1600.00
    END,
    CASE 
        WHEN c.cin = 'TEST_AB123456' THEN 45000.00
        WHEN c.cin = 'TEST_CD789012' THEN 60000.00
        WHEN c.cin = 'TEST_EF345678' THEN 36000.00
        WHEN c.cin = 'TEST_GH901234' THEN 54000.00
        ELSE 48000.00
    END,
    CASE 
        WHEN c.cin = 'TEST_AB123456' THEN 3000.00
        WHEN c.cin = 'TEST_CD789012' THEN 2000.00
        WHEN c.cin = 'TEST_EF345678' THEN 3600.00
        WHEN c.cin = 'TEST_GH901234' THEN 3600.00
        ELSE 1600.00
    END,
    1000.00,
    'S2',
    CASE 
        WHEN c.cin = 'TEST_AB123456' THEN 75.0
        WHEN c.cin = 'TEST_CD789012' THEN 85.0
        WHEN c.cin = 'TEST_EF345678' THEN 65.0
        WHEN c.cin = 'TEST_GH901234' THEN 70.0
        ELSE 80.0
    END
FROM clients c
WHERE c.cin IN ('TEST_AB123456', 'TEST_CD789012', 'TEST_EF345678', 'TEST_GH901234', 'TEST_IJ567890')
ON CONFLICT (reference_contrat, client_id) DO NOTHING;

-- 3. CRÉER LES DOSSIERS DU MOIS 7
-- Avec des statuts variés pour tester la logique de transition
-- Note: Utilise un cast conditionnel vers ENUM si disponible
INSERT INTO dossiers (
    credit_id, 
    statut_actuel, 
    date_creation, 
    mois_import, 
    est_recurrent,
    mois_precedent_statut
)
SELECT 
    cr.id,
    CASE 
        WHEN cr.reference_contrat = 'DOSS-TEST-001' THEN 'CONTACTE'
        WHEN cr.reference_contrat = 'DOSS-TEST-002' THEN 'PROMESSE'
        WHEN cr.reference_contrat = 'DOSS-TEST-003' THEN 'INJOIGNABLE'
        WHEN cr.reference_contrat = 'DOSS-TEST-004' THEN 'EN_ATTENTE'
        ELSE 'EN_COURS'
    END,
    CURRENT_DATE - INTERVAL '30 days',
    '2024-07',
    false,
    NULL
FROM credits cr
JOIN clients cl ON cr.client_id = cl.id
WHERE cl.cin IN ('TEST_AB123456', 'TEST_CD789012', 'TEST_EF345678', 'TEST_GH901234', 'TEST_IJ567890')
ON CONFLICT DO NOTHING;

-- 4. VÉRIFICATIONS
SELECT '✅ Données de test créées pour le mois 2024-07' as message;

-- Compter les dossiers
SELECT 
    '📊 Total dossiers mois 7' as info,
    COUNT(*) as nombre
FROM dossiers
WHERE mois_import = '2024-07';

-- Répartition par statut
SELECT 
    '📈 Répartition par statut' as info,
    d.statut_actuel,
    COUNT(*) as nombre
FROM dossiers d
WHERE d.mois_import = '2024-07'
GROUP BY d.statut_actuel
ORDER BY nombre DESC;

-- Liste détaillée
SELECT 
    '📋 Liste des dossiers créés' as titre,
    cr.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    cr.impaye_montant as total_du,
    cr.score
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.mois_import = '2024-07'
ORDER BY cr.reference_contrat;
