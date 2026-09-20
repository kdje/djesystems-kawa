import { useCallback, useEffect, useState } from "react";
import { getAuth } from "firebase/auth";

import { AppIcon } from "../../components/AppIcon";
import {
  getMyRetailers,
  type CustomerRetailersResponse,
  type CustomerRetailerRelation,
} from "../../api/customerApi";

function getStatusLabel(status: CustomerRetailerRelation["status"]) {
  switch (status) {
    case "PENDING":
      return "Demande en attente";

    case "APPROVED":
      return "Autorisation accordée";

    case "ACTIVE":
      return "Association active";

    case "REJECTED":
      return "Demande refusée";

    default:
      return status;
  }
}

function getRetailerDisplayName(retailerCode: string) {
  // Pour l'instant le backend ne nous renvoie que retailerCode.
  // Plus tard, retailer-service fournira le vrai nom/logo.
  return retailerCode;
}

export function RetailersPage() {
  const [data, setData] =
    useState<CustomerRetailersResponse | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const loadRetailers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const user = getAuth().currentUser;

      if (!user) {
        throw new Error(
          "Utilisateur Firebase non connecté."
        );
      }

      const response =
        await getMyRetailers(user);

      console.log(
        "[KAWA] Retailers response:",
        response
      );

      setData(response);

    } catch (err) {
      console.error(
        "[KAWA] Erreur chargement enseignes:",
        err
      );

      setError(
        err instanceof Error
          ? err.message
          : "Impossible de charger vos enseignes."
      );

    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadRetailers();
  }, [loadRetailers]);

  /*
   * Permet de rafraîchir les données lorsque
   * l'utilisateur revient dans l'application / onglet.
   */
  useEffect(() => {
    const handleFocus = () => {
      void loadRetailers();
    };

    window.addEventListener("focus", handleFocus);

    return () => {
      window.removeEventListener(
        "focus",
        handleFocus
      );
    };
  }, [loadRetailers]);

  const activeCount =
    data?.activeCount ?? 0;

  const pendingCount =
    data?.pendingCount ?? 0;

  const retailers =
    data?.retailers ?? [];

  return (
    <div className="kawa-page kawa-retailers-page">

      <section className="kawa-page-heading kawa-retailers-heading">
        <div>
          <span className="kawa-eyebrow">
            Enseignes
          </span>

          <h1>Mes enseignes</h1>

          <p>
            Gérez les enseignes autorisées à associer
            votre identité KAWA à leur programme fidélité.
          </p>
        </div>
      </section>

      <section className="kawa-retailer-summary-grid">

        <article className="kawa-summary-tile">

          <div className="kawa-summary-tile-icon">
            <AppIcon
              name="retailers"
              size={20}
            />
          </div>

          <div>
            <span>
              Associations actives
            </span>

            <strong>
              {loading ? "…" : activeCount}
            </strong>
          </div>

        </article>

        <article className="kawa-summary-tile">

          <div className="kawa-summary-tile-icon">
            <AppIcon
              name="bell"
              size={20}
            />
          </div>

          <div>
            <span>
              Demandes en attente
            </span>

            <strong>
              {loading ? "…" : pendingCount}
            </strong>
          </div>

        </article>

        <article className="kawa-summary-tile">

          <div className="kawa-summary-tile-icon">
            <AppIcon
              name="shield"
              size={20}
            />
          </div>

          <div>
            <span>Contrôle</span>
            <strong>100 % vous</strong>
          </div>

        </article>

      </section>

      <section className="kawa-panel kawa-retailer-list-panel">

        <div className="kawa-panel-header kawa-panel-header-centered-mobile">

          <div>
            <span className="kawa-eyebrow">
              Associations
            </span>

            <h2>
              Vos programmes fidélité
            </h2>
          </div>

          <span className="kawa-filter-pill">
            Toutes
          </span>

        </div>

        {loading && (
          <div className="kawa-retailer-empty">
            <p>
              Chargement de vos enseignes…
            </p>
          </div>
        )}

        {!loading && error && (
          <div className="kawa-retailer-empty">

            <div className="kawa-retailer-empty-visual">
              <div className="kawa-retailer-store-icon">
                <AppIcon
                  name="retailers"
                  size={32}
                />
              </div>
            </div>

            <h3>
              Impossible de charger vos enseignes
            </h3>

            <p>
              {error}
            </p>

            <button
              type="button"
              onClick={() =>
                void loadRetailers()
              }
            >
              Réessayer
            </button>

          </div>
        )}

        {!loading &&
          !error &&
          retailers.length === 0 && (

          <div className="kawa-retailer-empty">

            <div className="kawa-retailer-empty-visual">

              <div className="kawa-retailer-store-icon">
                <AppIcon
                  name="retailers"
                  size={32}
                />
              </div>

              <span className="kawa-retailer-connector" />

              <div className="kawa-retailer-kawa-icon">
                K
              </div>

            </div>

            <h3>
              Aucune enseigne associée
            </h3>

            <p>
              Votre liste se remplira automatiquement
              après votre première demande d’association.
            </p>

          </div>
        )}

        {!loading &&
          !error &&
          retailers.length > 0 && (

          <div className="kawa-retailer-summary-grid">

            {retailers.map((retailer) => (

              <article
                className="kawa-summary-tile"
                key={retailer.retailerCode}
              >

                <div className="kawa-summary-tile-icon">
                  <AppIcon
                    name={
                      retailer.status === "PENDING"
                        ? "bell"
                        : retailer.status === "ACTIVE"
                          ? "check"
                          : "retailers"
                    }
                    size={20}
                  />
                </div>

                <div>

                  <span>
                    {getStatusLabel(
                      retailer.status
                    )}
                  </span>

                  <strong>
                    {getRetailerDisplayName(
                      retailer.retailerCode
                    )}
                  </strong>

                </div>

              </article>

            ))}

          </div>
        )}

      </section>

      <section className="kawa-how-it-works">

        <div className="kawa-section-heading">

          <div>

            <span className="kawa-eyebrow">
              Simple et transparent
            </span>

            <h2>
              Comment une enseigne rejoint votre KAWA ?
            </h2>

          </div>

        </div>

        <div className="kawa-steps-grid">

          <article className="kawa-step-card">

            <span className="kawa-step-number">
              1
            </span>

            <div className="kawa-step-icon">
              <AppIcon
                name="card"
                size={21}
              />
            </div>

            <h3>
              Vous présentez votre QR
            </h3>

            <p>
              L’enseigne lit uniquement votre identifiant
              KAWA public.
            </p>

          </article>

          <article className="kawa-step-card">

            <span className="kawa-step-number">
              2
            </span>

            <div className="kawa-step-icon">
              <AppIcon
                name="bell"
                size={21}
              />
            </div>

            <h3>
              Vous recevez une demande
            </h3>

            <p>
              KAWA vous informe qu’une enseigne souhaite
              créer l’association.
            </p>

          </article>

          <article className="kawa-step-card">

            <span className="kawa-step-number">
              3
            </span>

            <div className="kawa-step-icon">
              <AppIcon
                name="check"
                size={21}
              />
            </div>

            <h3>
              Vous gardez la décision
            </h3>

            <p>
              Vous autorisez ou refusez. Rien n’est associé
              sans votre accord.
            </p>

          </article>

        </div>

      </section>

    </div>
  );
}