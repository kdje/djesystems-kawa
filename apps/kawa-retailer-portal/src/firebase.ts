import { getApp, getApps, initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

const config = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

export const firebaseConfigurationReady = Object.values(config).every(Boolean);

const firebaseApp = firebaseConfigurationReady
  ? getApps().length
    ? getApp()
    : initializeApp(config)
  : undefined;

export const auth = firebaseApp ? getAuth(firebaseApp) : undefined;
