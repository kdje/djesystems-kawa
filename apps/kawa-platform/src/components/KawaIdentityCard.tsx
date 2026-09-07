import { QRCodeSVG } from "qrcode.react";

type KawaIdentityCardProps = {
  publicKawaId: string;
  status?: string;
};

export function KawaIdentityCard({
  publicKawaId,
  status,
}: KawaIdentityCardProps) {
  const qrValue = `KAWA:1:${publicKawaId}`;

  return (
    <section className="card">
      <h1>KAWA</h1>

      <p>One identity. All your loyalty cards.</p>

      <div className="kawa-qr-wrapper">
        <QRCodeSVG
          value={qrValue}
          size={220}
          level="M"
        />
      </div>
      <p>
        <strong>KAWA ID</strong>
      </p>

      <code>{publicKawaId}</code>

      <p className="note">
        Permanent QR: not transaction-specific and not retailer-specific.
      </p>

      {status && (
        <span className="kawa-status">
          <span className="kawa-status-dot" />
          Status: <strong>{status}</strong>
        </span>
      )}
    </section>
  );
}