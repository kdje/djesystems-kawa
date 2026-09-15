import { AppIcon } from "../../components/AppIcon";

export function RetailersPage() {
  return (
    <div className="kawa-page kawa-retailers-page">
      <section className="kawa-page-heading kawa-retailers-heading">
        <div>
          <span className="kawa-eyebrow">Enseignes</span>
          <h1>Mes enseignes</h1>
          <p>Gérez les enseignes autorisées à associer votre identité KAWA à leur programme fidélité.</p>
        </div>
      </section>

      <section className="kawa-retailer-summary-grid">
        <article className="kawa-summary-tile">
          <div className="kawa-summary-tile-icon"><AppIcon name="retailers" size={20} /></div>
          <div><span>Associations actives</span><strong>0</strong></div>
        </article>
        <article className="kawa-summary-tile">
          <div className="kawa-summary-tile-icon"><AppIcon name="bell" size={20} /></div>
          <div><span>Demandes en attente</span><strong>0</strong></div>
        </article>
        <article className="kawa-summary-tile">
          <div className="kawa-summary-tile-icon"><AppIcon name="shield" size={20} /></div>
          <div><span>Contrôle</span><strong>100 % vous</strong></div>
        </article>
      </section>

      <section className="kawa-panel kawa-retailer-list-panel">
        <div className="kawa-panel-header kawa-panel-header-centered-mobile">
          <div>
            <span className="kawa-eyebrow">Associations</span>
            <h2>Vos programmes fidélité</h2>
          </div>
          <span className="kawa-filter-pill">Toutes</span>
        </div>

        <div className="kawa-retailer-empty">
          <div className="kawa-retailer-empty-visual">
            <div className="kawa-retailer-store-icon"><AppIcon name="retailers" size={32} /></div>
            <span className="kawa-retailer-connector" />
            <div className="kawa-retailer-kawa-icon">K</div>
          </div>
          <h3>Aucune enseigne associée</h3>
          <p>Votre liste se remplira automatiquement après votre première demande d’association acceptée.</p>
        </div>
      </section>

      <section className="kawa-how-it-works">
        <div className="kawa-section-heading">
          <div>
            <span className="kawa-eyebrow">Simple et transparent</span>
            <h2>Comment une enseigne rejoint votre KAWA ?</h2>
          </div>
        </div>

        <div className="kawa-steps-grid">
          <article className="kawa-step-card">
            <span className="kawa-step-number">1</span>
            <div className="kawa-step-icon"><AppIcon name="card" size={21} /></div>
            <h3>Vous présentez votre QR</h3>
            <p>L’enseigne lit uniquement votre identifiant KAWA public.</p>
          </article>
          <article className="kawa-step-card">
            <span className="kawa-step-number">2</span>
            <div className="kawa-step-icon"><AppIcon name="bell" size={21} /></div>
            <h3>Vous recevez une demande</h3>
            <p>KAWA vous informe qu’une enseigne souhaite créer l’association.</p>
          </article>
          <article className="kawa-step-card">
            <span className="kawa-step-number">3</span>
            <div className="kawa-step-icon"><AppIcon name="check" size={21} /></div>
            <h3>Vous gardez la décision</h3>
            <p>Vous autorisez ou refusez. Rien n’est associé sans votre accord.</p>
          </article>
        </div>
      </section>
    </div>
  );
}
