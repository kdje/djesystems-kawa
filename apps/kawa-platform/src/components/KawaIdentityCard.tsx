import { QRCodeSVG } from "qrcode.react";
import { AppIcon } from "./AppIcon";

type KawaIdentityCardProps = {
  publicKawaId: string;
  status?: string;
  compact?: boolean;
};

function shortId(value: string) {
  if (value.length <= 18) return value;
  return `${value.slice(0, 8)}…${value.slice(-8)}`;
}

export function KawaIdentityCard({ publicKawaId, status, compact = false }: KawaIdentityCardProps) {
  const qrValue = `KAWA:1:${publicKawaId}`;

  return (
    <section className={`kawa-identity-card${compact ? " kawa-identity-card-compact" : ""}`}>
      <div className="kawa-card-glow" />
      <div className="kawa-card-header">
        <div className="kawa-card-brand">
          <span className="kawa-card-logo">K</span>
          <span>KAWA</span>
        </div>
        {status && (
          <span className={`kawa-status-pill kawa-status-${status.toLowerCase()}`}>
            <span className="kawa-status-dot" />
            {status === "ACTIVE" ? "Actif" : status}
          </span>
        )}
      </div>

      <div className="kawa-card-body">
        <div className="kawa-qr-panel">
          <QRCodeSVG value={qrValue} size={compact ? 144 : 176} level="M" />
        </div>

        <div className="kawa-card-copy">
          <span className="kawa-eyebrow kawa-eyebrow-light">Votre identifiant universel</span>
          <h2>Une seule identité.<br />Toutes vos cartes.</h2>
          <p>Présentez ce QR code chez une enseigne partenaire pour retrouver votre fidélité sans chercher une carte.</p>

          <div className="kawa-id-block">
            <div>
              <span>KAWA ID</span>
              <strong title={publicKawaId}>{shortId(publicKawaId)}</strong>
            </div>
            <div className="kawa-verified-badge" title="Identité KAWA active">
              <AppIcon name="check" size={15} />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
