import {
  getApp,
  getApps,
  initializeApp
} from "firebase/app";

import {
  getAuth,
  indexedDBLocalPersistence,
  initializeAuth,
  type Auth,
} from "firebase/auth";

import { Capacitor } from "@capacitor/core";


const firebaseConfig = {
  apiKey:
    import.meta.env.VITE_FIREBASE_API_KEY,

  authDomain:
    import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,

  projectId:
    import.meta.env.VITE_FIREBASE_PROJECT_ID,

  storageBucket:
    import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,

  messagingSenderId:
    import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,

  appId:
    import.meta.env.VITE_FIREBASE_APP_ID,
};


if (
  !firebaseConfig.apiKey ||
  !firebaseConfig.authDomain ||
  !firebaseConfig.projectId ||
  !firebaseConfig.appId
) {
  throw new Error(
    "Firebase Web configuration is incomplete. Check .env.local."
  );
}


export const firebaseApp =
  getApps().length
    ? getApp()
    : initializeApp(firebaseConfig);


export const auth: Auth =
  Capacitor.isNativePlatform()
    ? initializeAuth(
        firebaseApp,
        {
          persistence:
            indexedDBLocalPersistence,
        }
      )
    : getAuth(firebaseApp);