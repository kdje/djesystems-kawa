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

## Image et déploiement DEV

Le Dockerfile produit une image Nginx autonome (port 80), avec fallback SPA et
proxy same-origin des requêtes `/api/retailer-portal/**` vers
`retailer-service:8083`. En DEV, le gateway sert le portail sous
`/retailer-portal/` sur le hostname API déjà configuré; il retire ce préfixe
avant de transmettre les requêtes au conteneur. Le portail utilise le même
hostname pour appeler le gateway, donc le CORS n'a pas à être élargi. Le compose
publie aussi directement le portail sur `http://localhost:5174` pour les
vérifications locales; le port 5173 reste réservé à `kawa-platform`.
L'image de déploiement exige les variables GitHub Environment `dev` suivantes:
`VITE_FIREBASE_API_KEY`, `VITE_FIREBASE_AUTH_DOMAIN`,
`VITE_FIREBASE_PROJECT_ID` et `VITE_FIREBASE_APP_ID`. Ce sont des valeurs de
configuration Web publiques, à configurer pour le projet Firebase utilisé; le
workflow échoue avant l'accès AWS si l'une manque. Aucun credential Firebase
Admin n'est utilisé pour construire le portail.

`VITE_RETAILER_API_BASE_URL` est facultative et doit rester vide pour le
déploiement same-origin recommandé. Si une URL API cross-origin est configurée,
définir la variable GitHub Environment `dev` `KAWA_RETAILER_PORTAL_CORS_ORIGINS`
avec l'origine exacte du portail; le workflow la transmet au runtime du
`retailer-service`. Ne pas utiliser `*`. Le proxy du gateway route
`/api/retailer-portal/**` vers le `retailer-service`, sans réécrire le chemin ni
contourner l'authentification Firebase.

## Connexion Firebase en DEV

La configuration de code du client ne peut pas autoriser un domaine ou activer
un fournisseur dans Firebase Console. Pour la connexion sur
`https://dev.kawa-retail.com/retailer-portal/`, l'administrateur du projet
Firebase doit vérifier dans **Authentication** que le fournisseur Google est
activé et que `dev.kawa-retail.com` figure parmi les domaines autorisés. Ajouter
aussi `api-dev.kawa-retail.com` si le portail est utilisé depuis cette origine.
Vérifier que le client OAuth Google autorise le domaine d'authentification
Firebase (`VITE_FIREBASE_AUTH_DOMAIN`) et son callback
`https://<authDomain>/__/auth/handler`, ainsi que les restrictions de référent
de la clé API pour le domaine du portail.

Le portail affiche maintenant les erreurs Firebase usuelles (domaine non
autorisé, fournisseur désactivé, popup bloquée/fermée, problème réseau et clé
API invalide) au lieu d'un message de connexion générique. Après une connexion
Firebase réussie, l'accès requiert toujours une association enseigne active
dans `retailer_user`; cette configuration et la console Firebase ne sont pas
modifiées par ce dépôt.

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
