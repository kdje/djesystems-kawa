import { AppIcon } from "../../components/AppIcon";

type NotificationStatus =
  | "idle"
  | "loading"
  | "enabled"
  | "error";

type Props = {
  onEnableNotifications: () => Promise<void>;
  notificationStatus: NotificationStatus;
};

export function NotificationsPage({
  onEnableNotifications,
  notificationStatus,
}: Props) {

  const isLoading =
    notificationStatus === "loading";

  const isEnabled =
    notificationStatus === "enabled";

  return (
    <div className="kawa-page">

      <section className="kawa-page-heading">
        <div>
          <span className="kawa-eyebrow">
            Notifications
          </span>

          <h1>
            Centre de notifications
          </h1>

          <p>
            Recevez les demandes d'association
            et les informations importantes de KAWA.
          </p>
        </div>

        <button
          className="kawa-primary-action"
          type="button"
          disabled={isLoading || isEnabled}
          onClick={() =>
            void onEnableNotifications()
          }
        >
          <AppIcon
            name="bell"
            size={18}
          />

          {isLoading
            ? "Activation…"
            : isEnabled
              ? "Notifications activées"
              : "Activer les notifications"}
        </button>

        {notificationStatus === "error" && (
          <p className="kawa-error">
            Impossible d'activer les notifications.
            Vérifiez les autorisations de l'application
            puis réessayez.
          </p>
        )}

        {notificationStatus === "enabled" && (
          <p>
            Les notifications sont activées
            sur cet appareil.
          </p>
        )}
      </section>

      <section className="kawa-panel kawa-large-empty-panel">

        <div className="kawa-empty-icon kawa-empty-icon--large">
          <AppIcon
            name="bell"
            size={30}
          />
        </div>

        <h2>
          Rien de nouveau
        </h2>

        <p>
          Les demandes de consentement reçues
          pendant votre session apparaîtront
          immédiatement dans KAWA.
        </p>

      </section>

    </div>
  );
}