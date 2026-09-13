import {
  getMessaging,
  getToken,
  onMessage,
  MessagePayload
} from "firebase/messaging";

import { firebaseApp } from "./firebase";

const messaging = getMessaging(firebaseApp);

export async function enableFirebaseNotifications(): Promise<string> {

  if (!("Notification" in window)) {
    throw new Error("Les notifications ne sont pas supportées par ce navigateur.");
  }

  if (!("serviceWorker" in navigator)) {
    throw new Error("Les Service Workers ne sont pas supportés.");
  }

  const permission = await Notification.requestPermission();

  if (permission !== "granted") {
    throw new Error("L'utilisateur n'a pas autorisé les notifications.");
  }

 const serviceWorkerRegistration =
  await navigator.serviceWorker.register(
    "/firebase-messaging-sw.js",
    {
      scope: "/firebase-cloud-messaging-push-scope/"
    }
  );

if (!serviceWorkerRegistration.active) {
  const worker =
    serviceWorkerRegistration.installing ??
    serviceWorkerRegistration.waiting;

  if (worker) {
    await new Promise<void>((resolve, reject) => {
      const timeout = window.setTimeout(() => {
        reject(
          new Error(
            "Timeout en attendant l'activation du service worker Firebase."
          )
        );
      }, 10000);

      worker.addEventListener("statechange", () => {
        if (worker.state === "activated") {
          window.clearTimeout(timeout);
          resolve();
        }
      });
    });
  }
}

if (!serviceWorkerRegistration.active) {
  throw new Error(
    "Le service worker Firebase n'est pas actif."
  );
}

  const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY;

  if (!vapidKey) {
    throw new Error("VITE_FIREBASE_VAPID_KEY est absent.");
  }

  const token = await getToken(messaging, {
    vapidKey,
    serviceWorkerRegistration
  });

  if (!token) {
    throw new Error("Firebase n'a retourné aucun FCM token.");
  }

  console.log("======================================");
  console.log("FCM TEST TOKEN");
  console.log(token);
  console.log("======================================");

  return token;
}

export function listenForFirebaseMessages(
  callback?: (payload: MessagePayload) => void
) {

  return onMessage(messaging, (payload) => {

    console.log("=== MESSAGE FCM REÇU ===");
    console.log("Payload complet :", payload);
    console.log("Titre :", payload.notification?.title);
    console.log("Body :", payload.notification?.body);
    console.log("Data :", payload.data);


    callback?.(payload);

    const title =
      payload.notification?.title ?? "KAWA";

    const body =
      payload.notification?.body ?? "Nouvelle notification";

    if (Notification.permission === "granted") {
      new Notification(title, {
        body
      });
    }
  });
}