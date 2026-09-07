import { FormEvent, useState } from "react";

import {
  loginWithEmailPassword,
  loginWithGoogle,
} from "../auth/authService";

export function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      setLoading(true);
      setError(null);

      await loginWithEmailPassword(email, password);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Impossible de vous connecter."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      setGoogleLoading(true);
      setError(null);

      await loginWithGoogle();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Connexion Google impossible."
      );
    } finally {
      setGoogleLoading(false);
    }
  };

  return (
    <main className="kawa-login-page">
      <section className="kawa-login-card">

        <div className="kawa-login-brand">
          <div className="kawa-logo-mark">K</div>

          <h1>KAWA</h1>

          <p>
            One identity.
            <br />
            All your loyalty cards.
          </p>
        </div>

        <form
          className="kawa-login-form"
          onSubmit={handleSubmit}
        >
          <label>
            Adresse e-mail

            <input
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(event.target.value)
              }
              placeholder="vous@exemple.com"
              autoComplete="email"
              required
            />
          </label>

          <label>
            Mot de passe

            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              placeholder="Votre mot de passe"
              autoComplete="current-password"
              required
            />
          </label>

          {error && (
            <div className="kawa-login-error">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="kawa-primary-button"
            disabled={loading}
          >
            {loading
              ? "Connexion..."
              : "Se connecter"}
          </button>
        </form>

        <div className="kawa-login-separator">
          <span>ou</span>
        </div>

        <button
          type="button"
          className="kawa-google-button"
          onClick={handleGoogleLogin}
          disabled={googleLoading}
        >
          <span className="google-g">G</span>

          {googleLoading
            ? "Connexion..."
            : "Continuer avec Google"}
        </button>
      </section>
    </main>
  );
}