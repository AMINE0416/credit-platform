# 🚀 Guide de Démarrage Rapide - Système d'Historique

## 📋 Prérequis
- Base de données PostgreSQL configurée
- Application backend Spring Boot démarrée
- Application frontend React démarrée
- Compte administrateur actif

## ⚡ Installation en 3 étapes

### Étape 1 : Créer la table historique

#### Option A : Via psql (Recommandé)
```bash
cd c:\Users\asus\Documents\credit-platform\scripts
psql -U postgres -d creditplatform -f migration_add_historique.sql
```

#### Option B : Via pgAdmin
1. Ouvrir pgAdmin
2. Connecter à la base `creditplatform`
3. Ouvrir Query Tool
4. Copier-coller le contenu de `scripts/migration_add_historique.sql`
5. Exécuter (F5)

#### Vérification
```sql
-- Vérifier que la table existe
SELECT COUNT(*) FROM dossier_historique;

-- Vérifier les index
SELECT indexname FROM pg_indexes WHERE tablename = 'dossier_historique';
```

### Étape 2 : Redémarrer le backend (si déjà lancé)
Le backend détecte automatiquement la nouvelle table au démarrage.

```bash
# Si vous utilisez Maven
mvn spring-boot:run

# Si vous utilisez Java directement
java -jar target/credit-platform-0.0.1-SNAPSHOT.jar
```

### Étape 3 : Tester l'interface d'archivage
1. Ouvrir l'application : `http://localhost:5173` (ou votre URL)
2. Se connecter en tant qu'admin
3. Aller dans **Admin → Import Excel**
4. Descendre jusqu'à la section **"Archiver avant import"**

✅ Si vous voyez la section avec les sélecteurs mois/année, l'installation est réussie !

## 🎯 Premier archivage

### Scénario : Archiver les données actuelles pour le mois 7

1. **Préparer l'archivage**
   - Dans la section "Archiver avant import"
   - Sélectionner **Mois : juillet (7)**
   - Sélectionner **Année : 2024**

2. **Lancer l'archivage**
   - Cliquer sur **"Archiver les dossiers actuels"**
   - Patienter quelques secondes

3. **Vérifier le résultat**
   ```
   ✅ Archivage réussi !
   123 dossiers archivés pour juillet 2024
   ```
   
   La période apparaît maintenant dans la liste des périodes archivées.

4. **Importer les nouvelles données**
   - Remonter dans la page
   - Déposer votre fichier Excel du mois 8
   - Cliquer sur "Lancer l'import"

## 🔍 Vérifier l'historique en base

```sql
-- Voir toutes les périodes archivées
SELECT 
    annee,
    mois,
    COUNT(*) as nb_dossiers,
    MIN(date_archivage) as date_archive
FROM dossier_historique
GROUP BY annee, mois
ORDER BY annee DESC, mois DESC;

-- Voir les dossiers d'un mois spécifique
SELECT 
    cin,
    nom || ' ' || prenom as nom_complet,
    reference_contrat,
    statut_dossier,
    ville,
    score
FROM dossier_historique
WHERE mois = 7 AND annee = 2024
ORDER BY score DESC NULLS LAST
LIMIT 10;

-- Comparer deux mois
SELECT 
    'Juillet' as periode, statut_dossier, COUNT(*) as nb
FROM dossier_historique
WHERE mois = 7 AND annee = 2024
GROUP BY statut_dossier

UNION ALL

SELECT 
    'Août' as periode, statut_dossier, COUNT(*) as nb
FROM dossier_historique
WHERE mois = 8 AND annee = 2024
GROUP BY statut_dossier

ORDER BY periode, statut_dossier;
```

## 🧪 Tests API

### Tester l'archivage via API

```bash
# 1. Se connecter et récupérer le token JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@eqdom.ma",
    "password": "Admin2024!"
  }'

# Copier le token JWT de la réponse

# 2. Archiver les dossiers pour juillet 2024
curl -X POST "http://localhost:8080/api/historique/archiver?mois=7&annee=2024" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"

# 3. Lister les périodes archivées
curl -X GET http://localhost:8080/api/historique/periodes \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"

# 4. Récupérer les dossiers d'un mois
curl -X GET "http://localhost:8080/api/historique/dossiers?mois=7&annee=2024" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

## 📊 Workflow typique mensuel

### Mois 7 → Mois 8

```mermaid
graph LR
    A[Base actuelle<br/>Mois 7] --> B[Archiver Mois 7]
    B --> C[Import Excel<br/>Mois 8]
    C --> D[Base actuelle<br/>Mois 8]
    D --> E[Historique Mois 7<br/>conservé]
```

### Étapes concrètes

1. **Fin juillet** : Base contient les données du mois 7
   
2. **Début août** :
   ```
   a. Archiver : Mois=7, Année=2024
   b. Import : Fichier_Mois8.xlsx
   c. Résultat : Base = Mois 8, Archive = Mois 7
   ```

3. **Fin août** : Base contient les données du mois 8

4. **Début septembre** :
   ```
   a. Archiver : Mois=8, Année=2024
   b. Import : Fichier_Mois9.xlsx
   c. Résultat : Base = Mois 9, Archive = Mois 7 + 8
   ```

## ❓ FAQ Rapide

### Q : Puis-je archiver plusieurs fois le même mois ?
**R** : Non, une contrainte empêche les doublons. Erreur : "Un archivage existe déjà pour ce mois"

### Q : L'archivage supprime-t-il les données actuelles ?
**R** : Non ! L'archivage **copie** les données. Rien n'est supprimé.

### Q : Combien de temps prend l'archivage ?
**R** : ~2-5 secondes pour 1000 dossiers (varie selon le serveur)

### Q : Puis-je archiver si la base est vide ?
**R** : Oui, mais 0 dossiers seront archivés. Pas très utile.

### Q : Comment restaurer un mois archivé ?
**R** : Cette fonctionnalité n'est pas encore disponible. Prévu en Phase 3.

### Q : L'historique consomme-t-il beaucoup d'espace ?
**R** : Environ ~5 Ko par dossier. Pour 10 000 dossiers × 12 mois = ~600 Mo/an

## 🐛 Problèmes courants

### Erreur : "Table dossier_historique doesn't exist"
```bash
# Solution : Exécuter la migration SQL
psql -U postgres -d creditplatform -f scripts/migration_add_historique.sql
```

### Erreur : "constraint unique_dossier_mois_annee"
```sql
-- Solution : Le mois est déjà archivé. Vérifier :
SELECT mois, annee, COUNT(*) 
FROM dossier_historique 
GROUP BY mois, annee;

-- Si besoin de réarchiver, supprimer l'ancien :
DELETE FROM dossier_historique WHERE mois = 7 AND annee = 2024;
```

### Le bouton d'archivage ne fait rien
1. Vérifier la console navigateur (F12)
2. Vérifier les logs backend
3. Vérifier que l'utilisateur est bien ADMIN

### Section d'archivage invisible
1. Vérifier que vous êtes sur la page **Admin → Import Excel**
2. Vider le cache navigateur (Ctrl+Shift+R)
3. Vérifier la version du frontend (doit inclure les modifications)

## 🎓 Prochaines étapes

Une fois le système d'historique fonctionnel :

1. ✅ Archiver régulièrement (chaque mois avant import)
2. 📊 Consulter l'historique via SQL
3. 📈 Analyser les tendances mensuelles
4. 🔍 Suivre l'évolution des dossiers
5. 📋 Générer des rapports comparatifs

## 📞 Support

En cas de problème :
1. Consulter `HISTORIQUE_DOSSIERS.md` pour la doc complète
2. Vérifier les logs : `app.log`
3. Tester les requêtes SQL manuellement
4. Contacter l'équipe technique

---

**Bonne utilisation du système d'historique ! 🎉**
