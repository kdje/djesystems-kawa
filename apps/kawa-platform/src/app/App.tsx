import { useEffect, useState } from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { LoginForm } from "../components/LoginForm";
import { AppLayout } from "../components/AppLayout";
import { useFirebaseUser } from "../hooks/useFirebaseUser";
import { useCurrentCustomer } from "../hooks/useCurrentCustomer";
import { registerNotificationDevice, respondToConsentRequest, type ConsentDecision } from "../api/customerApi";
import { enableFirebaseNotifications, listenForFirebaseMessages } from "../firebase/firebaseMessaging";
import { HomePage } from "../pages/pro/HomePage";
import { CardPage } from "../pages/pro/CardPage";
import { RetailersPage } from "../pages/pro/RetailersPage";
import { NotificationsPage } from "../pages/pro/NotificationsPage";
import { ProfilePage } from "../pages/pro/ProfilePage";

type KawaNotification = { title: string; body: string; eventId: string };

export default function App() {
  const { user, authLoading } = useFirebaseUser();
  const { customer, customerLoading, customerError } = useCurrentCustomer(user);
  const [notification, setNotification] = useState<KawaNotification | null>(null);
  const [consentLoading, setConsentLoading] = useState(false);

  useEffect(() => {
    return listenForFirebaseMessages((payload) => {
      const eventId = payload.data?.eventId;
      if (!eventId) return;
      setNotification({
        title: payload.notification?.title ?? "Nouvelle demande",
        body: payload.notification?.body ?? "Une enseigne souhaite associer votre identité KAWA.",
        eventId,
      });
    });
  }, []);

  const handleEnableNotifications = async () => {
    if (!user) return;
    const token = await enableFirebaseNotifications();
    await registerNotificationDevice(user, token);
  };

  const handleConsentDecision = async (decision: ConsentDecision) => {
    if (!user || !notification) return;
    try {
      setConsentLoading(true);
      await respondToConsentRequest(user, notification.eventId, decision);
      setNotification(null);
    } finally {
      setConsentLoading(false);
    }
  };

  if (authLoading) {
    return <div className="kawa-splash"><div className="kawa-logo-mark">K</div><span>Initialisation de KAWA…</span></div>;
  }
  if (!user) return <LoginForm />;

  return (
    <BrowserRouter>
      <AppLayout user={user} onEnableNotifications={handleEnableNotifications}>
        <Routes>
          <Route path="/" element={<HomePage user={user} customer={customer} loading={customerLoading} error={customerError} />} />
          <Route path="/carte" element={<CardPage customer={customer} loading={customerLoading} error={customerError} />} />
          <Route path="/enseignes" element={<RetailersPage />} />
          <Route path="/notifications" element={<NotificationsPage onEnableNotifications={handleEnableNotifications} />} />
          <Route path="/profil" element={<ProfilePage user={user} customer={customer} />} />
        </Routes>
      </AppLayout>

      {notification && (
        <div className="kawa-consent-modal-backdrop" role="presentation">
          <section className="kawa-consent-modal" role="dialog" aria-modal="true" aria-labelledby="consent-title">
            <div className="kawa-modal-icon">K</div>
            <span className="kawa-eyebrow">Autorisation demandée</span>
            <h2 id="consent-title">{notification.title}</h2>
            <p>{notification.body}</p>
            <div className="kawa-modal-actions">
              <button type="button" className="kawa-secondary-action" disabled={consentLoading} onClick={() => void handleConsentDecision("REJECTED")}>Refuser</button>
              <button type="button" className="kawa-primary-action" disabled={consentLoading} onClick={() => void handleConsentDecision("APPROVED")}>{consentLoading ? "Traitement…" : "Autoriser"}</button>
            </div>
          </section>
        </div>
      )}
    </BrowserRouter>
  );
}
