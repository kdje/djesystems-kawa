import type { User } from "firebase/auth";
import { Link } from "react-router-dom";
import type { Customer } from "../../types/Customer";
import { KawaIdentityCard } from "../../components/KawaIdentityCard";
import { AppIcon } from "../../components/AppIcon";

type Props = {
  user: User;
  customer: Customer | null;
  loading: boolean;
  error: string | null;
};

function firstName(user: User) {
  const displayName = user.displayName?.trim();
  if (displayName) return displayName.split(/\s+/)[0];
  const emailName = user.email?.split("@")[0] ?? "";
  return emailName ? emailName.split(/[._-]/)[0] : "";
}

export function HomePage({ user, customer, loading, error }: Props) {
  const name = firstName(user);

  return (
    <div className="kawa-page kawa-home-page">
      <section className="kawa-home-hero">
        <div>
          <span className="kawa-eyebrow">Votre espace KAWA</span>
          <h1>{name ? `Bonjour ${name}` : "Bonjour"} <span aria-hidden="true">👋</span></h1>
          <p>Votre identité fidélité universelle, simple à présenter et toujours sous votre contrôle.</p>
        </div>
        <Link className="kawa-primary-action kawa-hero-action" to="/carte">
          <AppIcon name="card" size={18} /> Afficher mon QR
        </Link>
      </section>

      {loading && <div className="kawa-state-card">Chargement de votre espace KAWA…</div>}
      {error && <div className="kawa-state-card kawa-state-error">{error}</div>}

      {!loading && !error && customer && (
        <>
          <section className="kawa-dashboard-hero-grid">
            <KawaIdentityCard publicKawaId={customer.publicKawaId} status={customer.status} compact />

            <div className="kawa-overview-stack">
              <article className="kawa-overview-card kawa-overview-card-primary">
                <div className="kawa-overview-icon"><AppIcon name="check" size={20} /></div>
                <div>
                  <span>Statut de votre identité</span>
                  <strong>{customer.status === "ACTIVE" ? "Carte KAWA active" : customer.status}</strong>
                  <p>Votre QR est prêt à être présenté chez une enseigne partenaire.</p>
                </div>
              </article>

              <article className="kawa-overview-card">
                <div className="kawa-overview-icon"><AppIcon name="shield" size={20} /></div>
                <div>
                  <span>Confidentialité</span>
                  <strong>Vous décidez qui accède à votre KAWA</strong>
                  <p>Chaque nouvelle association nécessite votre accord explicite.</p>
                </div>
              </article>
            </div>
          </section>

          <section className="kawa-section-heading">
            <div>
              <span className="kawa-eyebrow">En un coup d’œil</span>
              <h2>Votre espace fidélité</h2>
            </div>
          </section>

          <div className="kawa-home-content-grid">
            <section className="kawa-panel kawa-retailer-preview-panel">
              <div className="kawa-panel-header">
                <div>
                  <span className="kawa-eyebrow">Mes enseignes</span>
                  <h2>Programmes associés</h2>
                </div>
                <Link className="kawa-text-link" to="/enseignes">Voir tout <AppIcon name="arrow" size={16} /></Link>
              </div>

              <div className="kawa-empty-state kawa-empty-state-premium">
                <div className="kawa-empty-icon"><AppIcon name="retailers" size={24} /></div>
                <div>
                  <strong>Aucune enseigne liée pour le moment</strong>
                  <p>Lors de votre prochain passage chez une enseigne compatible, KAWA vous demandera votre autorisation avant toute association.</p>
                  <Link className="kawa-inline-cta" to="/enseignes">Découvrir le fonctionnement <AppIcon name="arrow" size={15} /></Link>
                </div>
              </div>
            </section>

            <section className="kawa-panel kawa-activity-panel">
              <div className="kawa-panel-header">
                <div>
                  <span className="kawa-eyebrow">Activité</span>
                  <h2>Tout est prêt</h2>
                </div>
              </div>

              <div className="kawa-activity-list">
                <div className="kawa-activity-item">
                  <span className="kawa-activity-dot" />
                  <div><strong>Identité KAWA disponible</strong><span>Votre QR permanent est actif.</span></div>
                </div>
                <div className="kawa-activity-item">
                  <span className="kawa-activity-dot kawa-activity-dot-soft" />
                  <div><strong>Consentements protégés</strong><span>Vous serez sollicité avant chaque nouvelle association.</span></div>
                </div>
              </div>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
