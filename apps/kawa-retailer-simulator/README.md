# KAWA Retailer Simulator

Application Android de développement qui simule une caisse retailer KAWA :

1. sélection de l'enseigne (AUCHAN, CARREFOUR, CORA ou LIDL) ;
2. authentification OAuth2 `client_credentials` avec les credentials de cette enseigne ;
3. scan du QR KAWA ou saisie manuelle du `publicKawaId` ;
4. appel `Wallet /resolve` ;
5. si le statut est `CONSENT_APPROVED`, création d'un `retailerCustomerId` simulé et appel automatique de `/link`.

## Configuration recommandée : local.properties

Les credentials ne sont plus à saisir au téléphone.

Dans `apps/kawa-retailer-simulator/` :

1. copier `local.properties.example` vers `local.properties` ;
2. compléter `KAWA_TENANT_ID`, `KAWA_WALLET_SCOPE` et les `CLIENT_ID/CLIENT_SECRET` de chaque enseigne ;
3. laisser `KAWA_WALLET_BASE_URL=https://api-dev.kawa-retail.com` ou l'adapter ;
4. reconstruire/réinstaller l'application après toute modification du fichier.

Exemple :

```properties
KAWA_TENANT_ID=<tenant-id>
KAWA_WALLET_SCOPE=api://<wallet-app-id>/.default
KAWA_WALLET_BASE_URL=https://api-dev.kawa-retail.com

AUCHAN_CLIENT_ID=<client-id-auchan>
AUCHAN_CLIENT_SECRET=<secret-value-auchan>
CARREFOUR_CLIENT_ID=<client-id-carrefour>
CARREFOUR_CLIENT_SECRET=<secret-value-carrefour>
LIDL_CLIENT_ID=<client-id-lidl>
LIDL_CLIENT_SECRET=<secret-value-lidl>
```

`local.properties` est explicitement exclu par `.gitignore`. Ne le commite pas.

> Utiliser la **Value** du client secret Entra, pas le Secret ID.

## Priorité des valeurs

Au premier lancement, l'écran est prérempli depuis `local.properties` (via `BuildConfig`).
Si tu modifies un champ dans l'écran et cliques sur **Enregistrer**, cette valeur locale Android prend priorité pour l'enseigne sélectionnée.

Les champs restent visibles volontairement pour faciliter le diagnostic. Le flux normal ne nécessite plus aucune saisie sur le téléphone.

## Important sur la sécurité

Cette application est un **simulateur de développement**. Les valeurs de `local.properties` sont injectées dans l'APK au moment du build. Un secret embarqué dans un APK peut être extrait ; ne distribue donc pas cet APK et n'utilise pas cette technique pour une application retailer de production.

## Build

Ouvrir ce dossier dans Android Studio ou exécuter :

```bash
./gradlew assembleDebug
```

Sous Windows :

```powershell
.\\gradlew.bat assembleDebug
```
