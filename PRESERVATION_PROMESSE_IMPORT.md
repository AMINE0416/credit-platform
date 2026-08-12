# Préservation des informations de promesse lors de l'import mensuel

## Problème résolu

Lorsqu'un dossier passe en statut **PROMESSE** ou **SUIVI_PROMESSE**, l'agent saisit :
- Le **montant** de la promesse
- La **date** de la promesse

Ces informations sont essentielles pour le suivi des dossiers. Le problème était que lors du processus mensuel suivant :
1. L'admin archive le mois
2. L'admin nettoie les données
3. L'admin importe le nouveau fichier Excel

Les informations de promesse (montant et date) **étaient perdues** car elles n'étaient pas préservées dans la table temporaire `dossiers_mois_precedent`.

## Solution implémentée

### 1. Modification du modèle `DossierMoisPrecedent`

**Fichier :** `src/main/java/org/eqdom/creditplatform/model/DossierMoisPrecedent.java`

Ajout de deux nouveaux champs :
```java
@Column(name = "date_promesse")
private LocalDate datePromesse;

@Column(name = "montant_promesse", precision = 15, scale = 2)
private BigDecimal montantPromesse;
```

### 2. Modification du service d'archivage

**Fichier :** `src/main/java/org/eqdom/creditplatform/service/ArchivageMensuelService.java`

Modification de la méthode `creerMoisPrecedent()` pour **copier** les informations de promesse :

```java
// PRÉSERVER les informations de promesse pour les dossiers PROMESSE et SUIVI_PROMESSE
if (dossier.getDatePromesse() != null) {
    precedent.setDatePromesse(dossier.getDatePromesse());
    log.debug("  📅 Promesse sauvegardée pour prochain mois - Date: {}", dossier.getDatePromesse());
}
if (dossier.getMontantPromesse() != null) {
    precedent.setMontantPromesse(java.math.BigDecimal.valueOf(dossier.getMontantPromesse()));
    log.debug("  💰 Promesse sauvegardée pour prochain mois - Montant: {}", dossier.getMontantPromesse());
}
```

### 3. Modification du service d'import

**Fichier :** `src/main/java/org/eqdom/creditplatform/service/ImportAvecDetectionService.java`

Modification de la méthode `creerDossier()` pour **restaurer** les informations de promesse depuis `dossiers_mois_precedent` :

#### a) Lors de la mise à jour d'un dossier existant :
```java
// PRÉSERVER LES INFORMATIONS DE PROMESSE si le dossier précédent en avait
if (dossierPrecedent != null) {
    if (dossierPrecedent.getDatePromesse() != null) {
        d.setDatePromesse(dossierPrecedent.getDatePromesse());
        log.info("  📅 Promesse préservée - Date: {}", dossierPrecedent.getDatePromesse());
    }
    if (dossierPrecedent.getMontantPromesse() != null) {
        d.setMontantPromesse(dossierPrecedent.getMontantPromesse().doubleValue());
        log.info("  💰 Promesse préservée - Montant: {}", dossierPrecedent.getMontantPromesse());
    }
}
```

#### b) Lors de la création d'un nouveau dossier récurrent :
```java
// PRÉSERVER LES INFORMATIONS DE PROMESSE si récurrent et si le dossier précédent en avait
if (isRecurrent && dossierPrecedent != null) {
    if (dossierPrecedent.getDatePromesse() != null) {
        dossier.setDatePromesse(dossierPrecedent.getDatePromesse());
        log.info("  📅 Promesse préservée - Date: {}", dossierPrecedent.getDatePromesse());
    }
    if (dossierPrecedent.getMontantPromesse() != null) {
        dossier.setMontantPromesse(dossierPrecedent.getMontantPromesse().doubleValue());
        log.info("  💰 Promesse préservée - Montant: {}", dossierPrecedent.getMontantPromesse());
    }
}
```

### 4. Migration SQL

**Fichier :** `scripts/add_promesse_to_mois_precedent.sql`

Script pour ajouter les colonnes à la table `dossiers_mois_precedent` :

```sql
ALTER TABLE dossiers_mois_precedent 
ADD COLUMN IF NOT EXISTS date_promesse DATE,
ADD COLUMN IF NOT EXISTS montant_promesse NUMERIC(15, 2);
```

## Instructions de déploiement

### Étape 1 : Appliquer la migration SQL

```bash
# Connexion à la base de données
psql -U votre_user -d votre_base -f scripts/add_promesse_to_mois_precedent.sql
```

Ou via pgAdmin :
1. Ouvrir pgAdmin
2. Se connecter à la base de données
3. Ouvrir l'éditeur de requête
4. Copier/coller le contenu du script
5. Exécuter

### Étape 2 : Recompiler le backend

```bash
# Depuis la racine du projet
mvn clean package -DskipTests
```

### Étape 3 : Redémarrer l'application

```bash
# Arrêter l'application en cours
# Puis démarrer avec le nouveau JAR
java -jar target/credit-platform-0.0.1-SNAPSHOT.jar
```

## Scénario de test

### Test complet du cycle

#### Mois 1 - Août 2026

1. **Import initial**
   - Importer le fichier Excel d'août 2026
   - Dossier 12345 créé avec statut EN_COURS

2. **Création d'une promesse**
   - Agent accède au dossier 12345
   - Change le statut vers PROMESSE
   - Saisit : Montant = 5000 MAD, Date = 2026-08-15
   - Sauvegarde
   - ✅ Vérifier : L'encadré de promesse s'affiche dans le détail du dossier

3. **Passage en suivi**
   - Agent change le statut de PROMESSE vers SUIVI_PROMESSE
   - ✅ Vérifier : L'encadré de promesse reste visible avec les mêmes infos

4. **Archivage du mois**
   - Admin clique sur "Archiver le mois"
   - ✅ Vérifier dans les logs : "📅 Promesse sauvegardée pour prochain mois"
   - ✅ Vérifier dans les logs : "💰 Promesse sauvegardée pour prochain mois"

#### Mois 2 - Septembre 2026

5. **Nettoyage et import**
   - Admin nettoie les données
   - Admin importe le fichier Excel de septembre 2026
   - Dossier 12345 détecté comme récurrent
   - ✅ Vérifier dans les logs : "📅 Promesse préservée"
   - ✅ Vérifier dans les logs : "💰 Promesse préservée"

6. **Vérification finale**
   - Accéder au détail du dossier 12345
   - ✅ Vérifier : L'encadré de promesse s'affiche toujours
   - ✅ Vérifier : Montant = 5000 MAD
   - ✅ Vérifier : Date = 2026-08-15

### Vérification en base de données

#### Vérifier la table temporaire après archivage

```sql
SELECT 
    numero_dossier,
    statut_final,
    date_promesse,
    montant_promesse,
    mois_origine
FROM dossiers_mois_precedent
WHERE date_promesse IS NOT NULL
ORDER BY numero_dossier;
```

#### Vérifier les dossiers après import

```sql
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    d.mois_import,
    d.est_recurrent
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.date_promesse IS NOT NULL
ORDER BY c.reference_contrat;
```

## Logs de suivi

Les logs permettent de suivre la préservation des promesses :

### Lors de l'archivage
```
2026-08-11 [DEBUG] Archivage: SUIVI_PROMESSE (statut actuel) → SUIVI_PROMESSE (statut pour prochain mois)
2026-08-11 [DEBUG]   📅 Promesse sauvegardée pour prochain mois - Date: 2026-08-15
2026-08-11 [DEBUG]   💰 Promesse sauvegardée pour prochain mois - Montant: 5000.0
```

### Lors de l'import
```
2026-09-01 [INFO] Dossier récurrent détecté: 12345 -> statut: SUIVI_PROMESSE
2026-09-01 [INFO]   📅 Promesse préservée - Date: 2026-08-15
2026-09-01 [INFO]   💰 Promesse préservée - Montant: 5000.0
2026-09-01 [INFO]   ✅ Nouveau dossier créé - ID: 234, Statut: SUIVI_PROMESSE
```

## Comportement selon les statuts

| Statut du mois N | Statut après import N+1 | Promesse préservée ? |
|-----------------|-------------------------|---------------------|
| PROMESSE        | SUIVI_PROMESSE          | ✅ OUI              |
| SUIVI_PROMESSE  | SUIVI_PROMESSE          | ✅ OUI              |
| EN_COURS        | ALERTE                  | ❌ Non (pas de promesse) |
| REGULARISE      | REGULARISE              | ❌ Non (dossier clos) |
| INJOIGNABLE     | SUIVI_INJOIGNABLE       | ❌ Non (pas de promesse) |

## Points importants

1. **Les promesses sont préservées uniquement pour les dossiers récurrents**
   - Si un dossier n'apparaît pas dans le nouvel Excel, ses informations sont perdues

2. **Les promesses restent visibles tant que le dossier est en SUIVI_PROMESSE**
   - Si l'agent change le statut vers EN_COURS ou autre, l'encadré disparaît
   - Mais les données restent en base de données (non supprimées)

3. **Modification manuelle possible**
   - L'agent peut toujours créer une nouvelle promesse
   - Les nouvelles valeurs écraseront les anciennes

4. **Historique conservé**
   - Les anciennes actions avec promesse restent dans l'historique
   - L'archivage mensuel conserve également ces informations dans `dossiers_archives`

## Améliorations futures possibles

1. **Historique des promesses**
   - Table dédiée `promesses` avec historique complet
   - Permet de suivre toutes les promesses d'un client

2. **Alertes automatiques**
   - Notification si date de promesse dépassée
   - Rappel automatique X jours avant la date

3. **Reporting des promesses**
   - Tableau de bord des promesses par agent
   - Taux de respect des promesses
   - Montant total des promesses en cours

4. **Modification sans changement de statut**
   - Permettre à l'agent de modifier montant/date de promesse
   - Sans avoir à créer une nouvelle action

## Support

En cas de problème :
1. Vérifier les logs de l'application (rechercher 📅 et 💰)
2. Vérifier que les colonnes existent dans `dossiers_mois_precedent`
3. Vérifier que l'archivage a bien été effectué avant l'import
4. Contacter l'équipe technique avec les logs

## Fichiers modifiés

- ✅ `src/main/java/org/eqdom/creditplatform/model/DossierMoisPrecedent.java`
- ✅ `src/main/java/org/eqdom/creditplatform/service/ArchivageMensuelService.java`
- ✅ `src/main/java/org/eqdom/creditplatform/service/ImportAvecDetectionService.java`
- ✅ `scripts/add_promesse_to_mois_precedent.sql`
