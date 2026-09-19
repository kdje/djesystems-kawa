import { Capacitor } from "@capacitor/core";
import {
  FirebaseMessaging
} from "@capacitor-firebase/messaging";

import {
  getMessaging,
  getToken,
  onMessage
} from "firebase/messaging";

import { firebaseApp } from "./firebase";


export type KawaMessagePayload = {
  notification?: {
    title?: string;
    body?: string;
  };
  data?: Record<string, string>;
};


export async function enableFirebaseNotifications():
  Promise<string> {

  /*
   * ANDROID / IOS
   */
  if (Capacitor.isNativePlatform()) {

    console.log(
      "[FCM] Native platform:",
      Capacitor.getPlatform()
    );

    const permission =
      await FirebaseMessaging.requestPermissions();

    console.log(
      "[FCM] Permission:",
      permission.receive
    );

    if (permission.receive !== "granted") {
      throw new Error(
        "Permission de notification refusée."
      );
    }

    const result =
      await FirebaseMessaging.getToken();

    console.log(
      "[FCM] Native token:",
      result.token
    );

    if (!result.token) {
      throw new Error(
        "Firebase n'a retourné aucun token FCM."
      );
    }

    return result.token;
  }


  /*
   * WEB
   */
  if (!("Notification" in window)) {
    throw new Error(
      "Les notifications ne sont pas supportées."
    );
  }

  if (!("serviceWorker" in navigator)) {
    throw new Error(
      "Les Service Workers ne sont pas supportés."
    );
  }

  const permission =
    await Notification.requestPermission();

  if (permission !== "granted") {
    throw new Error(
      "Permission de notification refusée."
    );
  }

  const registration =
    await navigator.serviceWorker.register(
      "/firebase-messaging-sw.js"
    );

  const vapidKey =
    import.meta.env.VITE_FIREBASE_VAPID_KEY;

  if (!vapidKey) {
    throw new Error(
      "VITE_FIREBASE_VAPID_KEY est absent."
    );
  }

  const messaging =
    getMessaging(firebaseApp);

  const result =
    await getToken(
      messaging,
      {
        vapidKey,
        serviceWorkerRegistration:
          registration
      }
    );

  if (!result) {
    throw new Error(
      "Firebase n'a retourné aucun token FCM."
    );
  }

  console.log(
    "[FCM] Web token:",
    result
  );

  return result;
}


export function listenForFirebaseMessages(
  callback?: (
    payload: KawaMessagePayload
  ) => void
) {

  /*
   * ANDROID / IOS
   */
  if (Capacitor.isNativePlatform()) {

    const listenerPromise =
      FirebaseMessaging.addListener(
        "notificationReceived",
        event => {

          console.log(
            "[FCM] Notification native:",
            event
          );

          callback?.({
            notification: {
              title:
                event.notification.title,
              body:
                event.notification.body
            },
            data:
              event.notification.data as
                Record<string, string>
                | undefined
          });
        }
      );

    return () => {
      void listenerPromise.then(
        listener => listener.remove()
      );
    };
  }


  /*
   * WEB
   */
  const messaging =
    getMessaging(firebaseApp);

  return onMessage(
    messaging,
    payload => {

      console.log(
        "[FCM] Notification web:",
        payload
      );

      callback?.({
        notification:
          payload.notification,
        data:
          payload.data
      });

      if (
        Notification.permission
        === "granted"
      ) {

        new Notification(
          payload.notification?.title
            ?? "KAWA",
          {
            body:
              payload.notification?.body
              ?? "Nouvelle notification"
          }
        );
      }
    }
  );
}