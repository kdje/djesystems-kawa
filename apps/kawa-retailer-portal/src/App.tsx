import {
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  type User,
} from "firebase/auth";
import { useEffect, useMemo, useState } from "react";
import {
  Link,
  Navigate,
  NavLink,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import { createPortalApi, PortalApiError } from "./api/portalApi";
import { authErrorMessage } from "./authErrorMessage";
import { auth, firebaseConfigurationReady } from "./firebase";
import type { PortalBilling, PortalRetailer, PortalSubscription } from "./types";

const apiBaseUrl = import.meta.env.VITE_RETAILER_API_BASE_URL ?? "";

function usePortalApi(user: User | null) {
  return useMemo(
    () =>
      createPortalApi(
        apiBaseUrl,
        async () => (user ? user.getIdToken() : null),
      ),
    [user],
  );
}

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    if (!auth) {
      setAuthReady(true);
      return;
    }
    return onAuthStateChanged(auth, (firebaseUser) => {
      setUser(firebaseUser);
      setAuthReady(true);
    });
  }, []);

  const api = usePortalApi(user);
  if (!authReady) return <div className="loading">Chargement de votre session…</div>;

  return (
    <Routes>
      <Route
        path="/login"
        element={
          user ? (
            <Navigate to="/dashboard" replace />
          ) : (
            <LoginPage onSignedIn={setUser} />
          )
        }
      />
      <Route
        path="/*"
        element={
          user ? (
            <PortalLayout user={user}>
              <Routes>
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<Dashboard api={api} />} />
                <Route path="/subscription" element={<SubscriptionPage api={api} />} />
                <Route path="/billing" element={<BillingPage api={api} />} />
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </PortalLayout>
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
    </Routes>
  );
}

function LoginPage({ onSignedIn }: { onSignedIn: (user: User) => void }) {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function completeLogin(login: () => Promise<{ user: User }>) {
    setBusy(true);
    setError("");
    try {
      const result = await login();
      onSignedIn(result.user);
      navigate("/dashboard", { replace: true });
    } catch (reason: unknown) {
      setError(authErrorMessage(reason));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="login-screen">
      <section className="login-card">
        <Link className="brand" to="/login" aria-label="KAWA accueil">
          <span className="brand-mark">K</span>
          <span>KAWA <small>PARTNER</small></span>
        </Link>
        <p className="eyebrow">ESPACE PROFESSIONNEL</p>
        <h1>Bienvenue dans votre portail</h1>
        <p className="muted">Connectez-vous pour gérer votre espace enseigne.</p>
        {!firebaseConfigurationReady && (
          <div className="alert">
            La configuration Firebase du portail est incomplète. Contactez votre
            administrateur.
          </div>
        )}
        {error && <div className="alert" role="alert">{error}</div>}
        <form
          onSubmit={(event) => {
            event.preventDefault();
            const firebaseAuth = auth;
            if (firebaseAuth) {
              void completeLogin(() =>
                signInWithEmailAndPassword(firebaseAuth, email.trim(), password),
              );
            }
          }}
        >
          <label>
            Adresse e-mail
            <input
              autoComplete="username"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <label>
            Mot de passe
            <input
              autoComplete="current-password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          <button className="primary-button" disabled={busy || !auth}>
            {busy ? "Connexion…" : "Se connecter"}
          </button>
        </form>
        <div className="divider"><span>ou</span></div>
        <button
          className="secondary-button"
          disabled={busy || !auth}
          onClick={() => {
            const firebaseAuth = auth;
            if (firebaseAuth) {
              void completeLogin(() =>
                signInWithPopup(firebaseAuth, new GoogleAuthProvider()),
              );
            }
          }}
        >
          Continuer avec Google
        </button>
        <p className="login-footnote">
          L’accès est réservé aux utilisateurs invités par KAWA.
        </p>
      </section>
    </main>
  );
}

function PortalLayout({
  user,
  children,
}: {
  user: User;
  children: React.ReactNode;
}) {
  const initials = (user.displayName ?? user.email ?? "K").slice(0, 1).toUpperCase();
  async function logoutUser() {
    if (auth) await signOut(auth);
  }

  return (
    <div className="portal-shell">
      <aside className="sidebar">
        <Link className="brand sidebar-brand" to="/dashboard">
          <span className="brand-mark">K</span><span>KAWA <small>PARTNER</small></span>
        </Link>
        <p className="nav-caption">ESPACE ENSEIGNE</p>
        <nav aria-label="Navigation principale">
          <NavLink to="/dashboard"><span>⌂</span> Vue d’ensemble</NavLink>
          <NavLink to="/subscription"><span>◇</span> Abonnement</NavLink>
          <NavLink to="/billing"><span>▤</span> Facturation</NavLink>
        </nav>
        <div className="sidebar-bottom">
          <span className="avatar">{initials}</span>
          <span className="user-email">{user.email ?? "Compte KAWA"}</span>
          <button className="icon-button" onClick={() => void logoutUser()} aria-label="Se déconnecter">
            ↗
          </button>
        </div>
      </aside>
      <div className="main-column">
        <header className="topbar">
          <div><span className="status-dot" /> Espace sécurisé</div>
          <button className="text-button" onClick={() => void logoutUser()}>Se déconnecter</button>
        </header>
        <main className="content">{children}</main>
      </div>
    </div>
  );
}

function Dashboard({ api }: { api: ReturnType<typeof createPortalApi> }) {
  const [retailer, setRetailer] = useState<PortalRetailer | null>(null);
  const [subscription, setSubscription] = useState<PortalSubscription | null>(null);
  const [error, setError] = useState("");
  useEffect(() => {
    void api.getRetailer().then(setRetailer).catch((reason: unknown) => {
      setError(messageFor(reason));
    });
    void api.getSubscription().then(setSubscription).catch((reason: unknown) => {
      if (!(reason instanceof PortalApiError) || reason.status !== 404) {
        setError(messageFor(reason));
      }
    });
  }, [api]);

  if (error) return <PageError message={error} />;
  return (
    <>
      <PageHeading eyebrow="VOTRE ESPACE" title="Vue d’ensemble">
        Retrouvez les informations principales de votre enseigne.
      </PageHeading>
      {!retailer ? <LoadingPanel /> : (
        <>
          <section className="welcome-card">
            <div>
              <p className="eyebrow light">ENSEIGNE CONNECTÉE</p>
              <h2>{retailer.name}</h2>
              <p>{retailer.code} <span className="separator">·</span> {retailer.countryCode}</p>
            </div>
            <span className="welcome-icon">✦</span>
          </section>
          <div className="section-title"><h2>Votre compte</h2><span>Informations actuelles</span></div>
          <div className="card-grid">
            <InfoCard title="Abonnement" value={subscription?.plan ?? "À configurer"} detail={
              subscription ? subscription.status : "Aucune offre renseignée"
            } icon="◇" to="/subscription" />
            <InfoCard title="Renouvellement" value={
              subscription?.renewalDate ? formatDate(subscription.renewalDate) : "—"
            } detail={subscription?.billingCycle ?? "Cycle non défini"} icon="◷" to="/subscription" />
            <InfoCard title="Votre rôle" value={roleLabel[retailer.role] ?? retailer.role} detail="Accès portail" icon="◎" />
          </div>
        </>
      )}
    </>
  );
}

function SubscriptionPage({ api }: { api: ReturnType<typeof createPortalApi> }) {
  const [subscription, setSubscription] = useState<PortalSubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    void api.getSubscription().then(setSubscription).catch((reason: unknown) => {
      if (!(reason instanceof PortalApiError) || reason.status !== 404) {
        setError(messageFor(reason));
      }
    }).finally(() => setLoading(false));
  }, [api]);
  return (
    <>
      <PageHeading eyebrow="OFFRE KAWA" title="Abonnement">
        Consultez les informations de votre offre. Les paiements en ligne ne sont pas activés.
      </PageHeading>
      {error ? <PageError message={error} /> : loading ? <LoadingPanel /> : subscription ? (
        <section className="detail-card">
          <div className="detail-header"><div><p className="eyebrow">OFFRE ACTUELLE</p><h2>{subscription.plan}</h2></div><span className="badge">{subscription.status}</span></div>
          <div className="detail-grid">
            <Detail label="Date de début" value={subscription.startDate ? formatDate(subscription.startDate) : "—"} />
            <Detail label="Prochaine échéance" value={subscription.renewalDate ? formatDate(subscription.renewalDate) : "—"} />
            <Detail label="Cycle de facturation" value={subscription.billingCycle ?? "—"} />
          </div>
          <p className="notice">La gestion de l’abonnement et les paiements seront disponibles dans une version ultérieure.</p>
        </section>
      ) : <EmptyState title="Aucun abonnement renseigné" detail="Les informations de votre offre seront affichées ici dès leur configuration par KAWA." />}
    </>
  );
}

function BillingPage({ api }: { api: ReturnType<typeof createPortalApi> }) {
  const [billing, setBilling] = useState<PortalBilling | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    void api.getBilling().then(setBilling).catch((reason: unknown) => {
      if (!(reason instanceof PortalApiError) || reason.status !== 404) {
        setError(messageFor(reason));
      }
    }).finally(() => setLoading(false));
  }, [api]);
  return (
    <>
      <PageHeading eyebrow="INFORMATIONS DE COMPTE" title="Facturation">
        Consultez les coordonnées de facturation de votre enseigne.
      </PageHeading>
      {error ? <PageError message={error} /> : loading ? <LoadingPanel /> : billing ? (
        <section className="detail-card">
          <p className="eyebrow">COMPTE DE FACTURATION</p>
          <h2>{billing.companyName}</h2>
          <div className="detail-grid">
            <Detail label="E-mail de facturation" value={billing.billingEmail} />
            <Detail label="Adresse" value={billing.billingAddress ?? "—"} />
            <Detail label="Compte externe" value={billing.externalAccountId ?? "Non associé"} />
          </div>
          <p className="notice">Les paiements et la gestion des factures ne sont pas activés dans cette version.</p>
        </section>
      ) : <EmptyState title="Coordonnées non renseignées" detail="Les informations de facturation seront affichées ici dès leur configuration par KAWA." />}
    </>
  );
}

function PageHeading({
  eyebrow,
  title,
  children,
}: {
  eyebrow: string;
  title: string;
  children: React.ReactNode;
}) {
  return <div className="page-heading"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{children}</p></div>;
}

function InfoCard({
  title,
  value,
  detail,
  icon,
  to,
}: {
  title: string;
  value: string;
  detail: string;
  icon: string;
  to?: string;
}) {
  const contents = <><span className="card-icon">{icon}</span><div><p>{title}</p><h3>{value}</h3><small>{detail}</small></div></>;
  return to ? <NavLink className="info-card" to={to}>{contents}<span className="card-arrow">↗</span></NavLink> : <section className="info-card">{contents}</section>;
}

function Detail({ label, value }: { label: string; value: string }) {
  return <div className="detail-item"><span>{label}</span><strong>{value}</strong></div>;
}

function EmptyState({ title, detail }: { title: string; detail: string }) {
  return <section className="empty-state"><span className="empty-icon">◇</span><h2>{title}</h2><p>{detail}</p></section>;
}

function LoadingPanel() {
  return <div className="loading-panel">Chargement des informations…</div>;
}

function PageError({ message }: { message: string }) {
  return <div className="alert page-alert" role="alert">{message}</div>;
}

function messageFor(error: unknown) {
  if (error instanceof PortalApiError && error.status === 403) {
    return "Aucun accès enseigne n’est associé à ce compte. Contactez votre administrateur KAWA.";
  }
  if (error instanceof PortalApiError && error.status === 401) {
    return "Votre session n’est plus valide. Déconnectez-vous puis reconnectez-vous.";
  }
  return error instanceof Error ? error.message : "Impossible de charger ces informations.";
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "long" }).format(new Date(`${value}T00:00:00`));
}

const roleLabel: Record<string, string> = {
  RETAILER_ADMIN: "Administrateur",
  BILLING_ADMIN: "Administration facturation",
  OPERATOR: "Opérateur",
  VIEWER: "Consultation",
};

export default App;
