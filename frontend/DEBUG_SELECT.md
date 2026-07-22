# Debug - Options de Select Non Visibles

## Symptôme
Quand on clique sur un champ select, les options ne s'affichent pas ou sont invisibles.

## Solutions appliquées

### 1. Forcer les styles CSS
- `color-scheme: light !important` pour désactiver le mode sombre
- `background-color: white !important` pour le fond
- `color: black !important` pour le texte
- `appearance: menulist !important` pour restaurer le style natif

### 2. Tests à faire

#### Test 1 : Vider le cache du navigateur
1. Ouvrir DevTools (F12)
2. Aller dans Network
3. Cocher "Disable cache"
4. Rafraîchir la page (Ctrl+F5)

#### Test 2 : Vérifier dans la console
1. Ouvrir la console (F12)
2. Chercher les logs "Transitions autorisées:"
3. Vérifier que des transitions sont bien chargées

#### Test 3 : Inspecteur d'éléments
1. Clic droit sur le select
2. "Inspecter l'élément"
3. Regarder les styles appliqués
4. Vérifier qu'il n'y a pas de `display: none` ou `opacity: 0`

#### Test 4 : Test avec le fichier HTML simple
Ouvrir `frontend/test-select.html` dans le navigateur pour tester un select simple.

### 3. Solutions alternatives si le problème persiste

#### Solution A : Remplacer par un custom dropdown
Si les selects natifs ne fonctionnent pas, on peut utiliser un composant custom.

#### Solution B : Vérifier les extensions du navigateur
Certaines extensions peuvent interférer avec les selects. Tester en mode navigation privée.

#### Solution C : Vérifier le zoom du navigateur
Un zoom différent de 100% peut causer des problèmes d'affichage.

### 4. Commandes utiles

```bash
# Redémarrer le serveur frontend
cd frontend
npm run dev

# Vider complètement node_modules et réinstaller
cd frontend
rm -rf node_modules
rm package-lock.json
npm install
npm run dev
```

### 5. Fichiers modifiés
- `frontend/src/index.css` - Styles globaux pour les selects
- `frontend/src/pages/DossierDetail.css` - Styles spécifiques
- `frontend/src/pages/DossierDetail.tsx` - État de chargement
- `frontend/src/pages/AdminDossiers.css` - Styles des filtres
- `frontend/src/pages/AdminDossiers.tsx` - État de chargement
- `frontend/src/pages/AgentDashboard.css` - État de chargement
- `frontend/src/pages/AgentDashboard.tsx` - État de chargement

### 6. Vérification backend

Si les options ne s'affichent toujours pas, vérifiez que le backend retourne bien les données :

```bash
# Tester l'endpoint des transitions (remplacer :id par un vrai ID)
curl http://localhost:8081/api/dossiers/1/transitions
```

La réponse devrait ressembler à :
```json
{
  "transitionsAutorisees": ["EN_COURS", "PROMESSE", "INJOIGNABLE"],
  "champsRequis": { ... }
}
```
