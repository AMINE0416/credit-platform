# Système d'Historique des Dossiers

## 📋 Vue d'ensemble

Le système d'historique permet d'archiver automatiquement l'état complet des dossiers avant chaque import mensuel de nouvelles données. Cela crée un snapshot mensuel de tous les dossiers avec leurs informations complètes (client, crédit, agent, statut, actions, etc.).

## 🎯 Cas d'usage

### Scenario typique
1. **Mois 7 (Juillet)** : Les données actuelles dans la base représentent le mois 7
2. **Avant import Mois 8** : Archiver les données du mois 7
3. **Import Mois 8** : Les nouvelles données écrasent/mettent à jour la base
4. **Résultat** : Vous avez l'historique du mois 7 ET les données actuelles du mois 8

## 🏗️ Architecture

### Base de données

#### Table principale : `dossier_historique`
```sql
CREATE TABLE dossier_historique (
    id BIGSERIAL PRIMARY KEY,
    
    -- Période
    mois INTEGER NOT NULL,
    annee INTEGER NOT NULL,
    date_archivage TIMESTAMP NOT NULL,
    
    -- Référence dossier original
    dossier_id_origine BIGINT,
    
    -- Toutes les données du dossier
    -- (client, crédit, statut, agent, actions...)
)
```

### Backend (Java/Spring Boot)

#### Service : `HistoriqueService`
- `archiverDossiersPourMois(mois, annee)` : Archive tous les dossiers actuels
- `getPeriodesArchivees()` : Liste toutes les périodes archivées
- `getDossiersArchives(mois, annee)` : Récupère les dossiers d'une période
- `getHistoriqueDossier(dossierIdOrigine)` : Historique complet d'un dossier

#### Controller : `HistoriqueController`
```
POST   /api/historique/archiver?mois=7&annee=2024
GET    /api/historique/periodes
GET    /api/historique/dossiers?mois=7&annee=2024
GET    /api/historique/dossier/{id}
```

### Frontend (React/TypeScript)

#### Service : `importService.ts`
```typescript
// Archiver avant import
await archiverDossiers(7, 2024);

// Récupérer les périodes archivées
const periodes = await getPeriodesArchivees();

// Récupérer les dossiers d'un mois
const dossiers = await getDossiersArchives(7, 2024);
```

#### Interface : `AdminImport.tsx`
Section dédiée dans la page d'import avec :
- Sélecteurs mois/année
- Bouton d'archivage
- Liste des périodes archivées
- Feedback de succès/erreur

## 📖 Guide d'utilisation

### 1. Archiver avant l'import (RECOMMANDÉ)

**Étape 1** : Aller dans Admin → Import Excel

**Étape 2** : Dans la section "Archiver avant import"
- Sélectionner le mois (ex: 7 pour juillet)
- Sélectionner l'année (ex: 2024)
- Cliquer sur "Archiver les dossiers actuels"

**Étape 3** : Attendre la confirmation
- Message de succès avec le nombre de dossiers archivés
- La période apparaît dans la liste des périodes archivées

**Étape 4** : Procéder à l'import du nouveau fichier Excel

### 2. Consulter l'historique

#### Via API
```bash
# Lister les périodes archivées
GET /api/historique/periodes

# Récupérer tous les dossiers du mois 7
GET /api/historique/dossiers?mois=7&annee=2024

# Historique d'un dossier spécifique
GET /api/historique/dossier/123
```

#### Via Interface (à venir)
Une page dédiée permettra de :
- Naviguer entre les mois archivés
- Comparer l'évolution des dossiers
- Exporter l'historique

## 🔒 Sécurité

- **Contrainte d'unicité** : Impossible d'archiver deux fois le même mois
- **Accès restreint** : Seuls les ADMIN peuvent archiver
- **Données immuables** : Les archives ne peuvent pas être modifiées

## 📊 Données archivées

Pour chaque dossier, l'archive contient :

### Informations Dossier
- ID du dossier original
- Statut actuel
- Agent assigné (email et nom)

### Informations Client
- CIN, nom, prénom
- Téléphones (1 et 2)
- Adresse, ville
- Code et libellé agence
- Adresse de cadrage

### Informations Crédit
- Référence contrat
- Type de crédit, segment
- Montants (mensualité, encours, impayé, provisions)
- Stage, DVR, rééchelonnement
- Immatriculation
- **Score de priorisation**

### Statistiques
- Nombre total d'actions effectuées
- Date de la dernière action

## 🛠️ Installation

### 1. Créer la table historique
```bash
psql -U postgres -d creditplatform -f scripts/create_historique_table.sql
```

### 2. Vérifier la table
```sql
SELECT COUNT(*) FROM dossier_historique;
SELECT DISTINCT mois, annee FROM dossier_historique ORDER BY annee DESC, mois DESC;
```

### 3. Tester l'archivage
```bash
# Via curl
curl -X POST "http://localhost:8080/api/historique/archiver?mois=7&annee=2024" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📈 Évolutions futures

### Phase 1 (Actuel)
- ✅ Archivage manuel avant import
- ✅ API complète de consultation
- ✅ Interface d'archivage dans AdminImport

### Phase 2 (À venir)
- 📅 Page dédiée "Historique" avec navigation mensuelle
- 📊 Tableaux de bord comparatifs mois par mois
- 📉 Graphiques d'évolution des KPIs
- 🔍 Recherche et filtrage dans l'historique

### Phase 3 (Future)
- 🤖 Archivage automatique avant chaque import
- 📧 Notifications automatiques des archivages
- 💾 Export Excel de l'historique
- 🔄 Restauration d'un mois archivé (rollback)

## 🐛 Dépannage

### Erreur : "Un archivage existe déjà pour ce mois"
**Solution** : Ce mois a déjà été archivé. Vérifiez les périodes archivées.

### Erreur : "Table dossier_historique n'existe pas"
**Solution** : Exécutez le script SQL de création de table.

### Aucun dossier archivé (count = 0)
**Causes possibles** :
- La base était vide au moment de l'archivage
- Erreur lors de l'archivage (vérifier les logs)

## 📝 Exemples de requêtes

### Comparer deux mois
```sql
-- Dossiers présents en juillet mais plus en août
SELECT h.reference_contrat, h.cin, h.nom
FROM dossier_historique h
WHERE h.mois = 7 AND h.annee = 2024
AND NOT EXISTS (
    SELECT 1 FROM dossier_historique h2
    WHERE h2.mois = 8 AND h2.annee = 2024
    AND h2.reference_contrat = h.reference_contrat
);
```

### Évolution d'un client
```sql
SELECT mois, annee, statut_dossier, nb_actions_total
FROM dossier_historique
WHERE cin = 'AB123456'
ORDER BY annee, mois;
```

### Statistiques mensuelles
```sql
SELECT 
    mois,
    annee,
    COUNT(*) as nb_dossiers,
    COUNT(DISTINCT agent_assigne_email) as nb_agents,
    AVG(score) as score_moyen
FROM dossier_historique
GROUP BY mois, annee
ORDER BY annee DESC, mois DESC;
```

## 🤝 Support

Pour toute question ou problème :
1. Vérifier les logs backend : `app.log`
2. Consulter les logs PostgreSQL
3. Tester les endpoints via Postman/curl
4. Contacter l'équipe technique

---

**Version** : 1.0.0  
**Date** : 2024  
**Auteur** : EQDOM Technical Team
