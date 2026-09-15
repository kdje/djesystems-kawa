# KAWA Platform V2

Cette V2 part de la version "pro" validée visuellement et fait évoluer principalement :

- l'Accueil en véritable dashboard produit ;
- la carte KAWA en version compacte sur l'Accueil ;
- les raccourcis vers le QR ;
- les blocs statut / confidentialité / activité ;
- la page Enseignes avec indicateurs, état vide professionnel et parcours en 3 étapes ;
- le responsive desktop / tablette / mobile.

La page `Ma carte`, Firebase Auth, FCM, le chargement du customer et la réponse aux consentements restent en place.

## Lancer

Conserver/recréer votre `.env.local`, puis :

```powershell
npm install
npm run dev
```

## Important

Les compteurs Enseignes restent à 0 tant qu'un endpoint backend ne fournit pas encore la liste persistée des associations client-enseigne. La V2 n'invente donc aucune association.
