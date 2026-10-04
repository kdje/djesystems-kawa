# KAWA Retailer Portal (V1)

Application web indépendante du client `kawa-platform` et du simulateur caisse.
Elle s'authentifie avec Firebase Authentication puis transmet exclusivement le
Firebase ID token à l'API du `retailer-service`.

## Développement

1. Copier `.env.example` vers `.env.local` et renseigner la configuration Web
   publique du projet Firebase utilisé par l'environnement. Aucun compte de
   démonstration, UID ou secret n'est inclus dans le dépôt.
2. Installer les dépendances avec `npm install`.
3. Lancer `npm run dev` (port local `5174`). Le proxy Vite transmet `/api` au
   `retailer-service` local sur `8083`.
4. Vérifier avec `npm test` et `npm run build`.

En déploiement, fournir `VITE_RETAILER_API_BASE_URL` selon l'URL de l'API; si
elle est vide, le portail utilise la même origine. Les valeurs Firebase `VITE_*`
sont la configuration cliente Firebase, pas des identifiants d'administration.

## Configuration backend Firebase/CORS

Le `retailer-service` exige `KAWA_FIREBASE_PROJECT_ID` et les identifiants
Application Default Credentials du service (Workload Identity en cloud, ou
`GOOGLE_APPLICATION_CREDENTIALS` pointant vers un fichier local non versionné).
Le SDK Firebase Admin vérifie la signature, l'émetteur, le projet et
l'expiration du Firebase ID token. Ne pas réutiliser la configuration Firebase
Web comme credentials serveur.

`KAWA_RETAILER_PORTAL_CORS_ORIGINS` est une liste séparée par virgules des
origines exactes du portail. La valeur locale par défaut est
`http://localhost:5174`; définir les origines déployées explicitement.

## Provisionnement V1

Il n'y a pas de compte test précréé, d'inscription ouverte ni de provisionnement
par l'interface. Un opérateur doit créer l'enseigne puis associer l'identité
Firebase connue de manière contrôlée dans `retailer_user` (provider `FIREBASE`,
tenant = ID projet Firebase, subject = UID Firebase, rôle et status `ACTIVE`).
Le schéma est ajouté par la migration Flyway V2. Ne jamais choisir l'enseigne
depuis une valeur cliente ni ajouter de UID de test dans une migration. Les
informations abonnement/facturation peuvent rester absentes; leurs écrans
affichent alors l'état non configuré. Aucun paiement n'est effectué.

Un utilisateur doit avoir exactement une association active pour accéder au
portail. Les identités sans association ou associées à plusieurs enseignes sont
refusées (403) jusqu'à la définition d'un parcours métier de sélection sûr.
