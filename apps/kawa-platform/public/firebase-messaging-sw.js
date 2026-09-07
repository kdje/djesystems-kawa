importScripts(
  "https://www.gstatic.com/firebasejs/11.0.2/firebase-app-compat.js"
);

importScripts(
  "https://www.gstatic.com/firebasejs/11.0.2/firebase-messaging-compat.js"
);

firebase.initializeApp({
  apiKey: "AIzaSyB9SUPWb5pltwVW5X8gAaMByXmrETNbMPo",
  authDomain: "kawa-dev-74031.firebaseapp.com",
  projectId: "kawa-dev-74031",
  storageBucket: "kawa-dev-74031.firebasestorage.app",
  messagingSenderId: "435772282016",
  appId: "TON_AP1:435772282016:web:adf0523ed239834fbfa5c5P_ID"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log(
    "[firebase-messaging-sw.js] Background message:",
    payload
  );

  const title =
    payload.notification?.title ??
    "KAWA";

  const options = {
    body:
      payload.notification?.body ??
      "Vous avez une nouvelle notification KAWA.",

    data: {
      ...(payload.data ?? {}),
      url: "/"
    }
  };

  return self.registration.showNotification(
    title,
    options
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();

  const url =
    event.notification.data?.url ?? "/";

  event.waitUntil(
    clients.matchAll({
      type: "window",
      includeUncontrolled: true
    }).then((clientList) => {

      for (const client of clientList) {
        if ("focus" in client) {
          client.navigate(url);
          return client.focus();
        }
      }

      return clients.openWindow(url);
    })
  );
});