# Ajout de l'adresse de cadrage et téléphone qualifié dans export

## Date : 12/08/2026

## Modifications effectuées

### 1. Frontend - DossierDetail.tsx

#### Ajout du champ "Adresse de cadrage"
- **Localisation** : Section de saisie pour les statuts PROMESSE et SUIVI_PROMESSE
- **Caractéristiques** :
  - Champ de type `textarea` (2 lignes)
  - Visible uniquement pour les transitions vers PROMESSE ou SUIVI_PROMESSE
  - Placeholder : "Adresse exacte pour cadrage terrain..."
  - Hint explicatif : "Cette adresse sera utilisée pour les actions de cadrage terrain"

#### Variables d'état ajoutées
```typescript
const [nouvelleAdresseCadrage, setNouvelleAdresseCadrage] = useState("");
```

#### Modification de la fonction saveAction
- Ajout du paramètre `nouvelleAdresseCadrage` dans l'appel API
- Envoi de la valeur uniquement si elle est remplie (trim)

#### Modification de la fonction resetForm
- Réinitialisation de `nouvelleAdresseCadrage` après enregistrement

### 2. Backend - ActionDossierRequest.java (DTO)

#### Nouveau champ ajouté
```java
private String nouvelleAdresseCadrage;
```

#### Getters/Setters
```java
public String getNouvelleAdresseCadrage() { return nouvelleAdresseCadrage; }
public void setNouvelleAdresseCadrage(String nouvelleAdresseCadrage) { 
    this.nouvelleAdresseCadrage = nouvelleAdresseCadrage; 
}
```

### 3. Backend - DossierService.java

#### Injection du ClientRepository
```java
@Autowired private ClientRepository clientRepository;
```

#### Logique de mise à jour de l'adresse de cadrage
Dans la méthode `ajouterAction()`, pour les statuts PROMESSE et SUIVI_PROMESSE :

```java
// Mettre à jour l'adresse de cadrage si fournie
if (request.getNouvelleAdresseCadrage() != null && !request.getNouvelleAdresseCadrage().trim().isEmpty()) {
    Client client = dossier.getCredit().getClient();
    client.setAdresseCadrage(request.getNouvelleAdresseCadrage().trim());
    clientRepository.save(client);
}
```

**Comportement** :
- L'adresse de cadrage est mise à jour au niveau du **Client** (table `clients`)
- La mise à jour se produit uniquement si une nouvelle adresse est fournie
- L'adresse est persistée pour être utilisée dans tous les exports futurs

### 4. Backend - ExcelExportService.java

#### Modification de la feuille "État actuel"

##### Ajout d'une colonne dans les headers
```java
"Telephone_Qualifie", "Compte_Rendu"
```

Nouvelle colonne : **Telephone_Qualifie** (après Date_Promesse)

##### Ajout de la valeur dans les données
```java
row.createCell(colNum++).setCellValue(nvl(derniereAction.getNouveauTelephone()));
row.createCell(colNum++).setCellValue(nvl(derniereAction.getCompteRendu()));
```

**Résultat** : Dans l'export Excel, on voit maintenant :
1. **Adresse_Cadrage** : Affichée dans la section "Données Client" (déjà existant)
2. **Telephone_Qualifie** : Affichée dans la section "Données saisies par l'agent" (nouvelle colonne)

#### Mise à jour du compteur de colonnes vides
```java
// Pas encore d'action : colonnes vides
for (int i = 0; i < 9; i++) {  // Changé de 8 à 9
    row.createCell(colNum++).setCellValue("");
}
```

## Flux de fonctionnement

### Cas d'usage : Enregistrement d'une promesse avec adresse de cadrage

1. **L'agent ouvre un dossier EN_COURS**
2. **L'agent sélectionne "Nouveau statut" → PROMESSE**
3. **Le formulaire affiche** :
   - Nature Action (obligatoire)
   - Motif Impayé (obligatoire)
   - Compte rendu (obligatoire)
   - Code résultat (obligatoire)
   - Montant promesse (obligatoire)
   - Date promesse (obligatoire)
   - Nouveau téléphone qualifié (optionnel)
   - **Adresse de cadrage (optionnel)**

4. **L'agent saisit l'adresse de cadrage** : "123 Rue Mohamed V, Immeuble Najah, Appt 5, Casablanca"
5. **L'agent clique sur "Enregistrer l'action"**
6. **Le backend** :
   - Crée une nouvelle `ActionDossier` avec tous les champs
   - Met à jour le dossier en statut PROMESSE
   - **Met à jour le champ `adresse_cadrage` du Client** avec la nouvelle valeur
7. **L'export Excel** :
   - Affiche l'adresse de cadrage dans la colonne "Adresse_Cadrage"
   - Affiche le téléphone qualifié dans la colonne "Telephone_Qualifie"

## Impact sur les exports Excel

### Feuille "État actuel"
Colonnes disponibles (dans l'ordre) :

**Données Client** :
- num_cin1, nom, prenom, cli_tel, TEL2, cli_adr
- Lib_Ville, cod_age, lib_age
- **Adresse_Cadrage** ← Mise à jour possible via PROMESSE/SUIVI_PROMESSE

**Données Crédit** :
- ANC_REFDO, From_Prov, GEST_CIN, CSP_PDT, Segment
- nbr_imp, Mensualite, encours, Mnt_Imp, prv_cum
- stage, DVR, Reecheance, Immatriculation, Score

**Gestion Dossier** :
- Agent_Email, Statut_Actuel, Date_Creation_Dossier

**Dernière Action** :
- Derniere_Action_Date, Nature_Action, Code_Resultat
- Motif_Impaye, Recidive, Montant_Promesse, Date_Promesse
- **Telephone_Qualifie** ← NOUVEAU
- Compte_Rendu

### Feuille "Historique statuts"
Pas de modification (les colonnes historiques restent inchangées)

## Points importants

### 1. Persistance de l'adresse de cadrage
- L'adresse de cadrage est stockée au niveau du **Client** (table `clients`)
- Une fois mise à jour, elle est visible dans tous les exports futurs
- Elle peut être modifiée à chaque nouvelle action PROMESSE/SUIVI_PROMESSE

### 2. Téléphone qualifié dans l'export
- Le téléphone qualifié provient de la **dernière action** du dossier
- Il est affiché dans l'export uniquement s'il a été saisi par l'agent
- Champ optionnel : peut être vide

### 3. Compatibilité
- Les dossiers existants sans adresse de cadrage afficheront "—" ou vide
- Pas de migration nécessaire : le champ existe déjà dans la base de données
- Les anciens exports restent compatibles

## Tests à effectuer

### Test 1 : Saisie de l'adresse de cadrage
1. Ouvrir un dossier EN_COURS
2. Passer en PROMESSE
3. Saisir une adresse de cadrage
4. Vérifier que l'adresse est enregistrée dans la table `clients`
5. Exporter en Excel
6. Vérifier que l'adresse apparaît dans la colonne "Adresse_Cadrage"

### Test 2 : Téléphone qualifié dans l'export
1. Ouvrir un dossier et saisir une action avec "Nouveau téléphone qualifié"
2. Exporter en Excel
3. Vérifier que le téléphone apparaît dans la colonne "Telephone_Qualifie"

### Test 3 : SUIVI_PROMESSE
1. Ouvrir un dossier en statut PROMESSE
2. Passer en SUIVI_PROMESSE
3. Modifier l'adresse de cadrage
4. Vérifier que la nouvelle adresse remplace l'ancienne
5. Exporter et vérifier

### Test 4 : Export sans adresse de cadrage
1. Ouvrir un dossier qui n'a jamais eu d'adresse de cadrage saisie
2. Exporter en Excel
3. Vérifier que la colonne "Adresse_Cadrage" est vide ou affiche "—"

## Commandes SQL pour vérifier

### Vérifier la structure de la table clients
```sql
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'clients' 
  AND column_name = 'adresse_cadrage';
```

### Vérifier les adresses de cadrage saisies
```sql
SELECT 
    c.id,
    c.cin,
    c.nom,
    c.prenom,
    c.adresse_cadrage,
    d.statut_actuel
FROM clients c
JOIN credits cr ON c.id = cr.client_id
JOIN dossiers d ON cr.id = d.credit_id
WHERE c.adresse_cadrage IS NOT NULL
  AND c.adresse_cadrage != '';
```

### Vérifier les téléphones qualifiés dans les actions
```sql
SELECT 
    ad.id,
    ad.nouveau_telephone,
    ad.date_action,
    d.statut_actuel,
    c.nom,
    c.prenom
FROM actions_dossier ad
JOIN dossiers d ON ad.dossier_id = d.id
JOIN credits cr ON d.credit_id = cr.id
JOIN clients c ON cr.client_id = c.id
WHERE ad.nouveau_telephone IS NOT NULL
ORDER BY ad.date_action DESC
LIMIT 20;
```

## Conclusion

✅ **Adresse de cadrage** : Champ de saisie ajouté pour PROMESSE et SUIVI_PROMESSE
✅ **Téléphone qualifié** : Ajouté dans l'export Excel (colonne "Telephone_Qualifie")
✅ **Adresse de cadrage dans export** : Déjà présente dans la colonne "Adresse_Cadrage"
✅ **Compatibilité** : Pas de migration nécessaire, fonctionnalité entièrement rétrocompatible

Les agents peuvent désormais saisir l'adresse de cadrage lors de la création d'une promesse, et cette information sera exportée dans Excel pour faciliter les actions de cadrage terrain.
