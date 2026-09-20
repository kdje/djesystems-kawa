import {
  useEffect,
  useState
} from "react";

import {
  BrowserRouter,
  Route,
  Routes
} from "react-router-dom";

import { LoginForm } from "../components/LoginForm";
import { AppLayout } from "../components/AppLayout";

import { useFirebaseUser } from "../hooks/useFirebaseUser";
import { useCurrentCustomer } from "../hooks/useCurrentCustomer";

import {
  registerNotificationDevice,
  respondToConsentRequest,
  type ConsentDecision
} from "../api/customerApi";

import {
  enableFirebaseNotifications,
  listenForFirebaseMessages
} from "../firebase/firebaseMessaging";

import { HomePage } from "../pages/pro/HomePage";
import { CardPage } from "../pages/pro/CardPage";
import { RetailersPage } from "../pages/pro/RetailersPage";
import { NotificationsPage } from "../pages/pro/NotificationsPage";
import { ProfilePage } from "../pages/pro/ProfilePage";


type KawaNotification = {
  title: string;
  body: string;
  eventId: string;
};

type NotificationStatus =
  | "idle"
  | "loading"
  | "enabled"
  | "error";


export default function App() {

  const {
    user,
    authLoading
  } = useFirebaseUser();

  const {
    customer,
    customerLoading,
    customerError
  } = useCurrentCustomer(user);


  const [
    notification,
    setNotification
  ] = useState<KawaNotification | null>(null);


  const [
    consentLoading,
    setConsentLoading
  ] = useState(false);


  const [
    notificationStatus,
    setNotificationStatus
  ] = useState<NotificationStatus>("idle");


  /*
   * Ecoute des notifications Firebase
   */
  useEffect(() => {

    return listenForFirebaseMessages(
      (payload) => {

        const eventId =
          payload.data?.eventId;

        if (!eventId) {
          return;
        }

        setNotification({
          title:
            payload.notification?.title
            ?? "Nouvelle demande",

          body:
            payload.notification?.body
            ?? "Une enseigne souhaite associer votre identité KAWA.",

          eventId
        });
      }
    );

  }, []);


  /*
   * Activation des notifications
   */
  const handleEnableNotifications =
    async (): Promise<void> => {

      if (!user) {
        console.error(
          "[FCM] Aucun utilisateur Firebase connecté."
        );

        setNotificationStatus("error");
        return;
      }

      try {

        setNotificationStatus("loading");

        console.log(
          "[FCM] Activation des notifications..."
        );


        /*
         * WEB :
         * - permission navigateur
         * - token FCM Web
         *
         * ANDROID :
         * - permission Android
         * - token FCM natif
         */
        const token =
          await enableFirebaseNotifications();


        console.log(
          "[FCM] Token obtenu:",
          token
        );


        /*
         * Enregistrement du device
         * dans customer-service.
         *
         * platform sera :
         * WEB / ANDROID / IOS
         */
        await registerNotificationDevice(
          user,
          token
        );


        console.log(
          "[FCM] Device enregistré dans customer-service."
        );


        setNotificationStatus(
          "enabled"
        );

      } catch (error) {

        console.error(
          "[FCM] Activation impossible:",
          error
        );

        setNotificationStatus(
          "error"
        );
      }
    };


  /*
   * Réponse à une demande
   * de consentement d'une enseigne
   */
  const handleConsentDecision =
    async (
      decision: ConsentDecision
    ) => {

      if (
        !user
        || !notification
      ) {
        return;
      }

      try {

        setConsentLoading(true);

        await respondToConsentRequest(
          user,
          notification.eventId,
          decision
        );

        setNotification(null);

      } catch (error) {

        console.error(
          "[CONSENT] Erreur lors du traitement:",
          error
        );

      } finally {

        setConsentLoading(false);
      }
    };


  /*
   * Initialisation Firebase
   */
  if (authLoading) {

    return (
      <div className="kawa-splash">

        <div className="kawa-logo-mark">
          K
        </div>

        <span>
          Initialisation de KAWA…
        </span>

      </div>
    );
  }


  /*
   * Non connecté
   */
  if (!user) {
    return <LoginForm />;
  }


  /*
   * Application connectée
   */
  return (

    <BrowserRouter>

      <AppLayout
        user={user}
        onEnableNotifications={
          handleEnableNotifications
        }
      >

        <Routes>

          <Route
            path="/"
            element={
              <HomePage
                user={user}
                customer={customer}
                loading={customerLoading}
                error={customerError}
              />
            }
          />


          <Route
            path="/carte"
            element={
              <CardPage
                customer={customer}
                loading={customerLoading}
                error={customerError}
              />
            }
          />


          <Route
            path="/enseignes"
            element={
              <RetailersPage />
            }
          />


          {/* UNE SEULE route notifications */}
          <Route
            path="/notifications"
            element={
              <NotificationsPage
                onEnableNotifications={
                  handleEnableNotifications
                }
                notificationStatus={
                  notificationStatus
                }
              />
            }
          />


          <Route
            path="/profil"
            element={
              <ProfilePage
                user={user}
                customer={customer}
              />
            }
          />

        </Routes>

      </AppLayout>


      {notification && (

        <div
          className="kawa-consent-modal-backdrop"
          role="presentation"
        >

          <section
            className="kawa-consent-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="consent-title"
          >

            <div className="kawa-modal-icon">
              K
            </div>

            <span className="kawa-eyebrow">
              Autorisation demandée
            </span>

            <h2 id="consent-title">
              {notification.title}
            </h2>

            <p>
              {notification.body}
            </p>


            <div className="kawa-modal-actions">

              <button
                type="button"
                className="kawa-secondary-action"
                disabled={consentLoading}
                onClick={() =>
                  void handleConsentDecision(
                    "REJECTED"
                  )
                }
              >
                Refuser
              </button>


              <button
                type="button"
                className="kawa-primary-action"
                disabled={consentLoading}
                onClick={() =>
                  void handleConsentDecision(
                    "APPROVED"
                  )
                }
              >
                {consentLoading
                  ? "Traitement…"
                  : "Autoriser"}
              </button>

            </div>

          </section>

        </div>
      )}

    </BrowserRouter>
  );
}