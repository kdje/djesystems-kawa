import { useEffect, useState } from "react";

import { KawaIdentityCard } from "../components/KawaIdentityCard";
import { LoginForm } from "../components/LoginForm";
import { UserMenu } from "../components/UserMenu";

import { useFirebaseUser } from "../hooks/useFirebaseUser";

import {
  getCurrentCustomer,
  registerNotificationDevice,
  respondToConsentRequest,
  type ConsentDecision,
} from "../api/customerApi";

import type { Customer } from "../types/Customer";

import {
  enableFirebaseNotifications,
  listenForFirebaseMessages,
} from "../firebase/firebaseMessaging";


type KawaNotification = {
  title: string;
  body: string;
  eventId: string;
};


export function KawaFirebaseDemoPage() {
  const { user, authLoading } =
    useFirebaseUser();

  const [customer, setCustomer] =
    useState<Customer | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [notification, setNotification] =
    useState<KawaNotification | null>(null);

  const [consentLoading, setConsentLoading] =
    useState(false);


  const handleEnableNotifications = async () => {
    try {
      if (!user) {
        throw new Error(
          "Utilisateur Firebase non connecté"
        );
      }

      const token =
        await enableFirebaseNotifications();

      await registerNotificationDevice(
        user,
        token
      );

      console.log(
        "Token FCM enregistré dans customer-service"
      );

    } catch (error) {
      console.error(
        "Erreur lors de l'activation des notifications",
        error
      );
    }
  };

  const handleConsentDecision = async (
      decision: ConsentDecision
    ) => {

      if (!user || !notification) {
        return;
      }

      try {
        setConsentLoading(true);

        await respondToConsentRequest(
          user,
          notification.eventId,
          decision
        );

        console.log(
          "Décision de consentement envoyée :",
          decision
        );

        setNotification(null);

      } catch (error) {

        console.error(
          "Impossible d'envoyer la décision",
          error
        );

      } finally {
        setConsentLoading(false);
      }
  };


  useEffect(() => {
    const unsubscribe =
      listenForFirebaseMessages((payload) => {

        const eventId =
          payload.data?.eventId;

        if (!eventId) {
          console.error(
            "Notification sans eventId",
            payload
          );
          return;
        }

        setNotification({
          title:
            payload.notification?.title
              ?? "Demande de consentement",

          body:
            payload.notification?.body
              ?? "Une enseigne demande votre consentement.",

          eventId,
        });
      });

    return unsubscribe;
  }, []);


  useEffect(() => {
    if (!user) {
      setCustomer(null);
      return;
    }

    const loadCustomer = async () => {
      try {
        setLoading(true);
        setError(null);

        const response =
          await getCurrentCustomer(user);

        setCustomer(response);

      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Impossible de récupérer le compte KAWA."
        );

      } finally {
        setLoading(false);
      }
    };

    loadCustomer();

  }, [user]);


  if (authLoading) {
    return (
      <div className="kawa-loading">
        Initialisation de KAWA...
      </div>
    );
  }


  if (!user) {
    return <LoginForm />;
  }


  return (
    <div className="kawa-app-shell">

      <header className="kawa-topbar">

        <div className="kawa-topbar-brand">
          <div className="kawa-mini-logo">
            K
          </div>

          <span>KAWA</span>
        </div>

        <div className="kawa-topbar-actions">

          <button
            type="button"
            className="kawa-notification-button"
            onClick={handleEnableNotifications}
            title="Activer les notifications"
            aria-label="Activer les notifications"
          >
            <span className="kawa-notification-icon">
              🔔
            </span>

            <span className="kawa-notification-label">
              Activer les notifications
            </span>
          </button>

          <UserMenu user={user} />

        </div>

      </header>


      <main className="kawa-dashboard">

        {loading && (
          <div className="kawa-loading">
            Chargement de votre identité KAWA...
          </div>
        )}

        {error && (
          <div className="kawa-dashboard-error">
            {error}
          </div>
        )}

        {!loading &&
          !error &&
          customer && (
            <KawaIdentityCard
              publicKawaId={customer.publicKawaId}
              status={customer.status}
            />
          )}

      </main>


      {notification && (
        <div
          className="kawa-toast"
          role="status"
          aria-live="polite"
        >

          <div className="kawa-toast-icon">
            🔔
          </div>

          <div className="kawa-toast-content">

            <div className="kawa-toast-title">
              {notification.title}
            </div>

            <div className="kawa-toast-body">
              {notification.body}
            </div>

             <div className="kawa-toast-actions">

                <button
                  type="button"
                  className="kawa-consent-button kawa-consent-accept"
                  disabled={consentLoading}
                  onClick={() =>
                    handleConsentDecision("APPROVED")
                  }
                >
                  ✓ Accepter
                </button>

                <button
                  type="button"
                  className="kawa-consent-button kawa-consent-reject"
                  disabled={consentLoading}
                  onClick={() =>
                    handleConsentDecision("REJECTED")
                  }
                >
                  Refuser
                </button>

              </div>


          </div>

          <button
            type="button"
            className="kawa-toast-close"
            onClick={() =>
              setNotification(null)
            }
            aria-label="Fermer la notification"
            title="Fermer"
          >
            ×
          </button>

        </div>
      )}

    </div>
  );
}