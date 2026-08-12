# 🚀 INSTALLATION RAPIDE - SYSTÈME D'ARCHIVAGE MENSUEL

## ⚡ Installation en 5 Minutes

### Étape 1 : Créer les tables SQL (2 min)

**Windows (PowerShell) :**
```powershell
# Option 1 : Script automatisé
.\scripts\test-archivage.ps1

# Option 2 : Manuel
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\create_archivage_system.sql
Remove-Item Env:\PGPASSWORD
```

**Linux/Mac :**
```bash
export PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts/create_archivage_system.sql
unset PGPASSWORD
```

### Étape 2 : Redémarrer l'application (1 min)

```bash
# Arrêter l'application si elle tourne
# Puis redémarrer
mvn spring-boot:run
```

Vérifier dans les logs :
```
✅ Pas d'erreur JPA
✅ "Mapped "{[/api/archivage/archiver],methods=[POST]}"
✅ "Mapped "{[/api/imports/avec-detection],methods=[POST]}"
```

### Étape 3 : Vérifier l'interface (1 min)

1. Se connecter en tant qu'admin
2. Aller sur `/admin/gestion-mensuelle`
3. Vérifier que la page s'affiche correctement

### Étape 4 : Test rapide (1 min)

**Via SQL (recommandé pour premier test) :**
```bash
# Windows
.\scripts\test-archivage.ps1

# Linux/Mac
psql -U postgres -d credit_platform -f scripts/test_complet_archivage.sql
```

**Via Interface :**
1. Page "Gestion Mensuelle"
2. Les sections doivent s'afficher sans erreur

---

## ✅ Vérifications Post-Installation

### 1. Tables créées
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');
```
**Attendu :** 3 lignes

### 2. Colonnes ajoutées
```sql
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'dossiers' 
AND column_name IN ('mois_import', 'est_recurrent', 'mois_precedent_statut');
```
**Attendu :** 3 lignes

### 3. Fonctions créées
```sql
SELECT routine_name FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name IN ('archiver_mois', 'nettoyer_dossiers', 'calculer_statut_recurrent');
```
**Attendu :** 3 lignes

### 4. Endpoints disponibles
```bash
# Vérifier dans les logs Spring Boot au démarrage
grep "api/archivage" logs/application.log
```
**Attendu :**
```
Mapped "{[/api/archivage/archiver],methods=[POST]}"
Mapped "{[/api/archivage/nettoyer/{mois}],methods=[DELETE]}"
Mapped "{[/api/archivage/mois],methods=[GET]}"
Mapped "{[/api/archivage/statistiques/{mois}],methods=[GET]}"
...
```

---

## 🧪 Test Complet (Optionnel - 15 minutes)

Pour tester le cycle complet avec données de test :

### Option 1 : Script automatisé (Recommandé)
```bash
# Windows
.\scripts\test-archivage.ps1

# Linux/Mac
psql -U postgres -d credit_platform -f scripts/test_complet_archivage.sql
```

### Option 2 : Étape par étape
```bash
# 1. Créer données mois 7
psql -U postgres -d credit_platform -f scripts/test_donnees_mois7.sql

# 2. Tester archivage
psql -U postgres -d credit_platform -f scripts/test_archivage_mois7.sql

# 3. Tester nettoyage
psql -U postgres -d credit_platform -f scripts/test_nettoyage_mois7.sql

# 4. Tester import avec détection
psql -U postgres -d credit_platform -f scripts/test_import_mois8_detection.sql
```

### Résultat attendu :
```
✅ 5 dossiers créés pour mois 7
✅ 5 dossiers archivés
✅ 5 entrées dans table détection
✅ Table dossiers nettoyée
✅ 5 dossiers importés pour mois 8 (3 récurrents + 2 nouveaux)
✅ Transitions automatiques :
   - CONTACTE → RELANCE
   - PROMESSE → SUIVI_PROMESSE
   - INJOIGNABLE → INJOIGNABLE
```

---

## 🚨 Résolution de Problèmes

### Erreur : "relation dossiers_archive does not exist"

**Cause :** Les tables n'ont pas été créées

**Solution :**
```bash
psql -U postgres -d credit_platform -f scripts/create_archivage_system.sql
```

### Erreur : "column mois_import does not exist"

**Cause :** Les colonnes n'ont pas été ajoutées à la table dossiers

**Solution :**
```sql
ALTER TABLE dossiers ADD COLUMN IF NOT EXISTS mois_import VARCHAR(7);
ALTER TABLE dossiers ADD COLUMN IF NOT EXISTS est_recurrent BOOLEAN DEFAULT FALSE;
ALTER TABLE dossiers ADD COLUMN IF NOT EXISTS mois_precedent_statut VARCHAR(50);
```

### Erreur : "Mapped not found" dans les logs Spring Boot

**Cause :** Le controller n'est pas chargé

**Solution :**
1. Vérifier que tous les fichiers Java sont compilés
2. Rebuild le projet : `mvn clean install`
3. Redémarrer l'application

### Page "Gestion Mensuelle" renvoie 404

**Cause :** Route frontend non configurée

**Solution :**
1. Vérifier que `AdminGestionMensuelle.tsx` existe
2. Vérifier que la route est dans `App.tsx`
3. Rebuild le frontend : `npm run build`

### Pas de données dans "Mois Archivés"

**Cause :** Normal si aucun mois n'a été archivé

**Solution :**
- Soit attendre d'avoir des dossiers réels à archiver
- Soit utiliser les scripts de test pour simuler

---

## 📚 Documentation Complète

Après l'installation, consultez :

1. **SYSTEME_ARCHIVAGE_README.md** - Vue d'ensemble complète
2. **GUIDE_DEMARRAGE_ARCHIVAGE.md** - Guide d'utilisation
3. **PLAN_TEST_ARCHIVAGE_MENSUEL.md** - Plan de test détaillé

---

## 🎯 Utilisation en Production

### Premier Mois (ex: Août 2024)

1. **Importer normalement** le fichier Excel du mois
   - Tous les dossiers seront marqués comme "nouveaux"
   - C'est normal pour le premier mois

2. **Travailler normalement** sur les dossiers

3. **Fin du mois :**
   - Cliquer "Archiver et Clôturer Août"
   - Télécharger le CSV généré
   - Cliquer "Nettoyer le Mois Août"

### Mois Suivants (ex: Septembre 2024)

1. **Importer avec détection** le fichier Excel
   - Les dossiers présents en Août seront automatiquement détectés
   - Leurs statuts seront appliqués selon l'historique

2. **Vérifier la détection :**
   - Page "Dossiers" → Filtre "Récurrents"
   - Vérifier que les statuts sont corrects

3. **Travailler et archiver** comme pour le premier mois

---

## ✅ Checklist de Déploiement Production

Avant de déployer en production :

- [ ] Backup complet de la base de données
- [ ] Tests validés en environnement de dev/staging
- [ ] Documentation lue par l'équipe admin
- [ ] Procédure mensuelle documentée en interne
- [ ] Point de rollback préparé
- [ ] Monitoring en place (logs, erreurs)
- [ ] Formation de l'équipe admin effectuée

---

## 🎉 C'est Prêt !

Le système est maintenant installé et prêt à l'emploi.

**Prochaine étape :** Attendre la fin du mois pour faire votre premier archivage réel ! 🚀

En cas de problème, consulter les fichiers de documentation ou les logs de l'application.
