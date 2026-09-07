import { useState } from "react";
import type { User } from "firebase/auth";

import { logout } from "../auth/authService";

type UserMenuProps = {
  user: User;
};

function getInitials(user: User): string {
  const name = user.displayName?.trim();

  if (name) {
    const parts = name
      .split(/\s+/)
      .filter(Boolean);

    if (parts.length >= 2) {
      return (
        parts[0][0] +
        parts[parts.length - 1][0]
      ).toUpperCase();
    }

    return name.substring(0, 2).toUpperCase();
  }

  const emailName =
    user.email?.split("@")[0] ?? "KA";

  const parts = emailName
    .split(/[._-]+/)
    .filter(Boolean);

  if (parts.length >= 2) {
    return (
      parts[0][0] +
      parts[1][0]
    ).toUpperCase();
  }

  return emailName
    .substring(0, 2)
    .toUpperCase();
}

export function UserMenu({
  user,
}: UserMenuProps) {
  const [open, setOpen] = useState(false);
  const [logoutLoading, setLogoutLoading] =
    useState(false);

  const initials = getInitials(user);

  const handleLogout = async () => {
    try {
      setLogoutLoading(true);

      await logout();
    } finally {
      setLogoutLoading(false);
    }
  };

  return (
    <div className="kawa-user-menu">
      <button
        type="button"
        className="kawa-avatar"
        onClick={() => setOpen(!open)}
        aria-label="Compte utilisateur"
      >
        {initials}
      </button>

      {open && (
        <div className="kawa-user-dropdown">

          <div className="kawa-user-information">
            {user.displayName && (
              <strong>
                {user.displayName}
              </strong>
            )}

            <span>{user.email}</span>
          </div>

          <div className="kawa-dropdown-divider" />

          <button
            type="button"
            className="kawa-logout-menu-item"
            onClick={handleLogout}
            disabled={logoutLoading}
          >
            <svg
              viewBox="0 0 24 24"
              width="18"
              height="18"
              aria-hidden="true"
            >
              <path
                d="M10 17l5-5-5-5M15 12H3M14 3h5a2 2 0 012 2v14a2 2 0 01-2 2h-5"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>

            {logoutLoading
              ? "Déconnexion..."
              : "Se déconnecter"}
          </button>
        </div>
      )}
    </div>
  );
}