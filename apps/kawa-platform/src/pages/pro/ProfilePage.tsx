import type { User } from "firebase/auth";
import type { Customer } from "../../types/Customer";

type Props = { user: User; customer: Customer | null };

export function ProfilePage({ user, customer }: Props) {
  return (
    <div className="kawa-page">
      <section className="kawa-page-heading">
        <div>
          <span className="kawa-eyebrow">Profil</span>
          <h1>Mon compte</h1>
          <p>Les informations principales associées à votre identité KAWA.</p>
        </div>
      </section>

      <section className="kawa-panel kawa-profile-panel">
        <div className="kawa-profile-row"><span>Nom</span><strong>{user.displayName || "Non renseigné"}</strong></div>
        <div className="kawa-profile-row"><span>E-mail</span><strong>{user.email || customer?.email || "Non renseigné"}</strong></div>
        <div className="kawa-profile-row"><span>Statut</span><strong>{customer?.status === "ACTIVE" ? "Actif" : customer?.status || "—"}</strong></div>
        <div className="kawa-profile-row"><span>Identifiant KAWA</span><strong className="kawa-profile-id">{customer?.publicKawaId || "—"}</strong></div>
      </section>
    </div>
  );
}
