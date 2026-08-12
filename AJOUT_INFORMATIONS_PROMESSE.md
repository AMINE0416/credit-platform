# Ajout des informations de promesse aux dossiers

## Objectif
Ajouter la date et le montant de promesse au niveau du dossier pour les statuts PROMESSE et SUIVI_PROMESSE, permettant aux admins et agents de visualiser ces informations dans les détails du dossier.

## Modifications effectuées

### 1. Backend - Modèle Dossier
**Fichier:** `src/main/java/org/eqdom/creditplatform/model/Dossier.java`

Ajout de deux nouveaux champs :
- `datePromesse` (LocalDate) : Date de la promesse de paiement
- `montantPromesse` (Double) : Montant promis par le client

```java
@Column(name = "date_promesse")
private LocalDate datePromesse;

@Column(name = "montant_promesse")
private Double montantPromesse;
```

### 2. Backend - Migration SQL
**Fichier:** `scripts/add_promesse_fields.sql`

Script SQL pour ajouter les colonnes à la base de données :
```sql
ALTER TABLE dossiers 
ADD COLUMN IF NOT EXISTS date_promesse DATE,
ADD COLUMN IF NOT EXISTS montant_promesse NUMERIC(15, 2);
```

### 3. Backend - Service DossierService
**Fichier:** `src/main/java/org/eqdom/creditplatform/service/DossierService.java`

Modification de la méthode `ajouterAction()` pour enregistrer les informations de promesse au niveau du dossier lors des transitions vers PROMESSE ou SUIVI_PROMESSE :

```java
if (nouveauStatut == StatutDossier.PROMESSE || nouveauStatut == StatutDossier.SUIVI_PROMESSE) {
    if (request.getDatePromesse() != null) {
        dossier.setDatePromesse(request.getDatePromesse());
    }
    if (request.getMontantPromesse() != null) {
        dossier.setMontantPromesse(request.getMontantPromesse().doubleValue());
    }
}
```

### 4. Backend - DTO DossierDetailDTO
**Fichier:** `src/main/java/org/eqdom/creditplatform/dto/DossierDetailDTO.java`

Ajout des champs dans le DTO pour exposer les informations via l'API :
```java
private String datePromesse;
private Double montantPromesse;

// Dans le constructeur
this.datePromesse = dossier.getDatePromesse() != null ? dossier.getDatePromesse().toString() : null;
this.montantPromesse = dossier.getMontantPromesse();
```

### 5. Frontend - Interface TypeScript
**Fichier:** `frontend/src/services/dossierService.ts`

Mise à jour de l'interface `DossierDetail` :
```typescript
export interface DossierDetail {
    dossierId: number;
    statut: string;
    client: any;
    credit: any;
    nbActions: number;
    datePromesse?: string | null;
    montantPromesse?: number | null;
}
```

### 6. Frontend - Composant DossierDetail
**Fichier:** `frontend/src/pages/DossierDetail.tsx`

Ajout d'un encadré visuel pour afficher les informations de promesse :
- Pour le statut PROMESSE : Affichage avec icône 🤝
- Pour le statut SUIVI_PROMESSE : Affichage avec icône 📅

L'encadré affiche :
- Le montant de la promesse (formaté en MAD)
- La date de la promesse (formatée en français)

## Instructions de déploiement

### 1. Appliquer la migration SQL
```bash
psql -U votre_user -d votre_base -f scripts/add_promesse_fields.sql
```

### 2. Recompiler le backend
```bash
mvn clean package -DskipTests
```

### 3. Redémarrer l'application
```bash
# Arrêter l'ancienne version
# Démarrer la nouvelle version avec le JAR généré
java -jar target/credit-platform-0.0.1-SNAPSHOT.jar
```

### 4. Rebuilder le frontend
```bash
cd frontend
npm run build
```

## Fonctionnement

### Création d'une promesse
1. L'agent accède au détail d'un dossier
2. Sélectionne le nouveau statut "PROMESSE" ou "SUIVI_PROMESSE"
3. Remplit les champs obligatoires :
   - Date de la promesse
   - Montant de la promesse
   - Autres champs requis selon la transition
4. Enregistre l'action
5. Les informations sont automatiquement enregistrées :
   - Dans l'action (table `actions_dossier`)
   - Dans le dossier (table `dossiers`)

### Visualisation
1. Admin ou Agent accède au détail du dossier
2. Si le statut est PROMESSE ou SUIVI_PROMESSE :
   - Un encadré coloré s'affiche en haut de la page
   - Affiche le montant et la date de la promesse
3. L'encadré reste visible tant que le dossier garde ce statut

## Rétrocompatibilité

Le système maintient la compatibilité avec les anciennes promesses :
- Si `dossier.datePromesse` et `dossier.montantPromesse` existent : utilise ces valeurs
- Sinon, pour SUIVI_PROMESSE : continue à utiliser `getDernierePromesse()` (méthode existante)

## Tests à effectuer

### Test 1 : Création d'une nouvelle promesse
1. Sélectionner un dossier avec statut EN_COURS
2. Changer le statut vers PROMESSE
3. Remplir date et montant de promesse
4. Enregistrer
5. Vérifier que l'encadré de promesse s'affiche correctement

### Test 2 : Suivi d'une promesse
1. Dossier en statut PROMESSE
2. Changer vers SUIVI_PROMESSE
3. Vérifier que l'encadré de promesse s'affiche toujours
4. Vérifier que les informations sont correctes

### Test 3 : Vérification en base de données
```sql
SELECT 
    id, 
    statut_actuel, 
    date_promesse, 
    montant_promesse 
FROM dossiers 
WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE');
```

### Test 4 : Anciens dossiers
1. Vérifier qu'un ancien dossier SUIVI_PROMESSE (créé avant cette modification)
2. Continue à afficher les informations via getDernierePromesse()

## Amélioration future possible

- Ajouter un historique des promesses (table dédiée)
- Permettre de modifier la date/montant de promesse sans changer de statut
- Ajouter des alertes si la date de promesse est dépassée
- Exporter les promesses dans les rapports Excel
