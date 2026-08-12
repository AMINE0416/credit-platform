# ⚡ COMMANDES RAPIDES - Archivage Mensuel

## 🎯 Pour Démarrer MAINTENANT

```powershell
# Se placer dans le dossier du projet
cd c:\Users\asus\Documents\credit-platform

# 1. Corriger le type ENUM
.\scripts\fix-enum-type.ps1

# 2. Tester tout le système
.\scripts\test-complet-automatique.ps1
```

✅ **C'est tout !** Ces 2 commandes vont :
- Créer le type ENUM si nécessaire
- Créer toutes les tables
- Compiler le projet
- Exécuter tous les tests
- Afficher les résultats

---

## 🚀 Pour Démarrer l'Application

```powershell
# Backend
mvn spring-boot:run
```

```powershell
# Frontend (dans un autre terminal)
cd frontend
npm run dev
```

**Accès :** `http://localhost:5173` → Login: `admin` / `admin`

---

## 🧪 Tests Manuels Rapides

### Test SQL Direct

```powershell
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\test_complet_archivage.sql
Remove-Item Env:\PGPASSWORD
```

### Test API

```powershell
# Login
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin"}'
$token = $response.token

# Lister les mois archivés
Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/mois" -Method GET -Headers @{ "Authorization" = "Bearer $token" }
```

---

## 📊 Vérification Rapide

```sql
-- Dans psql
psql -U postgres -d credit_platform

-- Vérifier les résultats
SELECT 'Archives mois 7' as test, COUNT(*) as resultat FROM dossiers_archive WHERE mois_archive = '2024-07'
UNION ALL
SELECT 'Dossiers mois 8', COUNT(*) FROM dossiers WHERE mois_import = '2024-08'
UNION ALL
SELECT 'Récurrents mois 8', COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true;
```

**Attendu :** 5, 5, 3

---

## 🆘 En Cas de Problème

### Erreur Type ENUM
```powershell
.\scripts\fix-enum-type.ps1
```

### Erreur Compilation
```powershell
mvn clean compile -DskipTests
```

### Erreur Tables
```powershell
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\create_archivage_system.sql
Remove-Item Env:\PGPASSWORD
```

---

## 📚 Documentation

- **README_ARCHIVAGE.md** - Vue d'ensemble complète
- **DEMARRAGE_RAPIDE.md** - Guide détaillé
- **CORRECTION_TYPE_ENUM.md** - Problème ENUM
- **PLAN_TEST_FINAL_SANS_EMAIL.md** - Tests complets

---

**C'est simple ! 🎉**
