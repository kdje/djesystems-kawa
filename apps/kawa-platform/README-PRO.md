# KAWA Platform - refonte UI professionnelle

Cette version conserve les briques techniques existantes (Firebase Auth, customer-service, FCM, consentement) et remplace la page de démonstration par une vraie structure applicative.

## Ce qui change

- vraie navigation desktop avec sidebar
- navigation mobile en barre basse
- tableau de bord d'accueil
- nouvelle carte KAWA avec QR code
- pages Ma carte, Enseignes, Notifications et Profil
- modale professionnelle pour les demandes de consentement
- design system KAWA centralisé dans `src/shared/styles.css`
- conservation de Firebase, du chargement du customer, de FCM et de l'API de consentement

## Lancer en local

Depuis le dossier `kawa-platform` :

```powershell
npm install
npm run dev
```

Puis ouvrir l'URL Vite habituelle (par défaut `http://localhost:5173`).

## Variables d'environnement

Le fichier `.env.local` d'origine n'est volontairement pas inclus dans ce ZIP afin de ne pas redistribuer tes secrets. Garde ton fichier `.env.local` actuel ou recrée-le à partir de `.env.example`.

## Fichiers principaux ajoutés

- `src/components/AppLayout.tsx`
- `src/components/AppIcon.tsx`
- `src/pages/pro/HomePage.tsx`
- `src/pages/pro/CardPage.tsx`
- `src/pages/pro/RetailersPage.tsx`
- `src/pages/pro/NotificationsPage.tsx`
- `src/pages/pro/ProfilePage.tsx`

`src/app/App.tsx` utilise maintenant React Router et orchestre les fonctionnalités existantes.
