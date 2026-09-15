import type { Customer } from "../../types/Customer";
import { KawaIdentityCard } from "../../components/KawaIdentityCard";

type Props = { customer: Customer | null; loading: boolean; error: string | null };

export function CardPage({ customer, loading, error }: Props) {
  return (
    <div className="kawa-page">
      <section className="kawa-page-heading">
        <div>
          <span className="kawa-eyebrow">Ma carte</span>
          <h1>Votre carte KAWA</h1>
          <p>Un QR permanent, conçu pour vous identifier auprès des enseignes partenaires.</p>
        </div>
      </section>
      {loading && <div className="kawa-state-card">Chargement de votre carte…</div>}
      {error && <div className="kawa-state-card kawa-state-error">{error}</div>}
      {!loading && !error && customer && <KawaIdentityCard publicKawaId={customer.publicKawaId} status={customer.status} />}
    </div>
  );
}
