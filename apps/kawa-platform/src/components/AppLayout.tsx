import type { ReactNode } from "react";
import type { User } from "firebase/auth";
import { NavLink } from "react-router-dom";
import { AppIcon } from "./AppIcon";
import { UserMenu } from "./UserMenu";

type AppLayoutProps = {
  user: User;
  children: ReactNode;
  onEnableNotifications: () => Promise<void>;
};

const navigation = [
  { to: "/", label: "Accueil", icon: "home" as const, end: true },
  { to: "/carte", label: "Ma carte", icon: "card" as const },
  { to: "/enseignes", label: "Enseignes", icon: "retailers" as const },
  { to: "/notifications", label: "Notifications", icon: "bell" as const },
  { to: "/profil", label: "Profil", icon: "profile" as const },
];

export function AppLayout({ user, children, onEnableNotifications }: AppLayoutProps) {
  return (
    <div className="kawa-app-shell">
      <aside className="kawa-sidebar">
        <div className="kawa-brand-lockup">
          <div className="kawa-logo-mark kawa-logo-mark-small">K</div>
          <div>
            <div className="kawa-brand-name">KAWA</div>
            <div className="kawa-brand-caption">Votre fidélité, simplement.</div>
          </div>
        </div>

        <nav className="kawa-sidebar-nav" aria-label="Navigation principale">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `kawa-nav-link${isActive ? " kawa-nav-link-active" : ""}`
              }
            >
              <AppIcon name={item.icon} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="kawa-sidebar-footer">
          <div className="kawa-security-note">
            <AppIcon name="shield" size={18} />
            <div>
              <strong>Identité protégée</strong>
              <span>Vous gardez le contrôle de vos autorisations.</span>
            </div>
          </div>
        </div>
      </aside>

      <div className="kawa-main-column">
        <header className="kawa-topbar">
          <div className="kawa-mobile-brand">
            <div className="kawa-logo-mark kawa-logo-mark-mobile">K</div>
            <strong>KAWA</strong>
          </div>

          <div className="kawa-topbar-actions">
            <button
              type="button"
              className="kawa-icon-button"
              onClick={() => void onEnableNotifications()}
              title="Activer les notifications"
              aria-label="Activer les notifications"
            >
              <AppIcon name="bell" size={19} />
            </button>
            <UserMenu user={user} />
          </div>
        </header>

        <main className="kawa-main-content">{children}</main>
      </div>

      <nav className="kawa-bottom-nav" aria-label="Navigation mobile">
        {navigation.slice(0, 4).map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `kawa-bottom-link${isActive ? " kawa-bottom-link-active" : ""}`
            }
          >
            <AppIcon name={item.icon} size={21} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
