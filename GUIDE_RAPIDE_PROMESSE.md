# Guide rapide - Préservation des promesses

## ⚡ Installation en 3 étapes

### 1️⃣ Appliquer la migration SQL

```bash
psql -U postgres -d credit_platform -f scripts/add_promesse_to_mois_precedent.sql
```

### 2️⃣ Recompiler l'application

```bash
mvn clean package -DskipTests
```

### 3️⃣ Redémarrer l'application

```bash
# Arrêter l'application en cours (Ctrl+C)
# Puis redémarrer
java -jar target/credit-platform-0.0.1-SNAPSHOT.jar
```

## ✅ Test rapide

### Créer une promesse de test

```sql
-- 1. Trouver un dossier EN_COURS
SELECT d.id, c.reference_contrat, d.statut_actuel
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel = 'EN_COURS'
LIMIT 1;

-- 2. Ajouter une promesse (remplacer <ID> par l'ID du dossier trouvé)
UPDATE dossiers 
SET 
    statut_actuel = 'PROMESSE',
    date_promesse = '2026-08-15',
    montant_promesse = 5000.00
WHERE id = <ID>;
```

### Simuler l'archivage et le nouvel import

```sql
-- 3. Vider la table temporaire
TRUNCATE TABLE dossiers_mois_precedent;

-- 4. Insérer dans la table temporaire (simuler l'archivage)
INSERT INTO dossiers_mois_precedent (
    numero_dossier, 
    statut_final, 
    mois_origine,
    date_promesse,
    montant_promesse
)
SELECT 
    c.reference_contrat,
    'SUIVI_PROMESSE',
    '2026-08',
    d.date_promesse,
    d.montant_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.date_promesse IS NOT NULL;

-- 5. Vérifier que les données sont bien dans la table temporaire
SELECT * FROM dossiers_mois_precedent;
```

### Tester l'import via l'interface

1. Ouvrir l'application web
2. Se connecter en tant qu'Admin
3. Aller dans "Gestion Mensuelle"
4. Cliquer sur "Importer un nouveau mois"
5. Sélectionner un fichier Excel
6. Vérifier dans les logs : 📅 et 💰

### Vérifier le résultat

```sql
-- Voir les dossiers avec promesse préservée
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    d.est_recurrent
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.date_promesse IS NOT NULL;
```

## 🔍 Diagnostics

### Vérifier que les colonnes existent

```bash
# Windows PowerShell
.\scripts\test-preservation-promesse.ps1
```

Ou manuellement :

```sql
SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'dossiers_mois_precedent';
```

### Vérifier les logs

Chercher dans les logs de l'application :
- `📅 Promesse sauvegardée` = Archivage OK
- `📅 Promesse préservée` = Import OK
- `💰 Promesse sauvegardée` = Archivage OK
- `💰 Promesse préservée` = Import OK

## 📖 Documentation complète

Voir [PRESERVATION_PROMESSE_IMPORT.md](PRESERVATION_PROMESSE_IMPORT.md) pour :
- Explication détaillée du problème
- Architecture de la solution
- Scénarios de test complets
- Dépannage

## ❓ Questions fréquentes

**Q: Les anciennes promesses sont-elles perdues ?**
R: Non, elles restent dans l'historique des actions. Seule la visualisation au niveau du dossier nécessite cette nouvelle fonctionnalité.

**Q: Que se passe-t-il si je modifie une promesse ?**
R: Les nouvelles valeurs écrasent les anciennes dans le dossier. L'historique dans `actions_dossier` est conservé.

**Q: La promesse est-elle préservée si le dossier change de statut ?**
R: Oui, les valeurs restent en base mais l'encadré n'est affiché que pour les statuts PROMESSE et SUIVI_PROMESSE.

**Q: Que se passe-t-il pour un nouveau dossier (non récurrent) ?**
R: Il n'aura pas de promesse automatique. L'agent devra en créer une manuellement si nécessaire.

## 🆘 Support

En cas de problème :
1. Vérifier que la migration SQL a bien été appliquée
2. Vérifier que l'application a été recompilée
3. Consulter les logs de l'application
4. Exécuter le script de test : `.\scripts\test-preservation-promesse.ps1`
