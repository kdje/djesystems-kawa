import { AppIcon } from "../../components/AppIcon";

type Props = { onEnableNotifications: () => Promise<void> };

export function NotificationsPage({ onEnableNotifications }: Props) {
  return (
    <div className="kawa-page">
      <section className="kawa-page-heading">
        <div>
          <span className="kawa-eyebrow">Notifications</span>
          <h1>Centre de notifications</h1>
          <p>Recevez les demandes d’association et les informations importantes de KAWA.</p>
        </div>
        <button className="kawa-primary-action" type="button" onClick={() => void onEnableNotifications()}>
          <AppIcon name="bell" size={18} /> Activer les notifications
        </button>
      </section>

      <section className="kawa-panel kawa-large-empty-panel">
        <div className="kawa-empty-icon kawa-empty-icon-large"><AppIcon name="bell" size={30} /></div>
        <h2>Rien de nouveau</h2>
        <p>Les demandes de consentement reçues pendant votre session apparaîtront immédiatement dans KAWA.</p>
      </section>
    </div>
  );
}
