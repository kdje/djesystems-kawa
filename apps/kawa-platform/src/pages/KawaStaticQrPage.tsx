import {QRCodeSVG} from 'qrcode.react'; const PUBLIC_ID='d73f8cb29aed4cc4941885b31e8d1327';
export function App()
{
    return <main className="page"><section className="card"><h1>KAWA</h1><p>One identity. All your loyalty cards.</p>
           <QRCodeSVG value={`KAWA:1:${PUBLIC_ID}`} size={220}/><code>{PUBLIC_ID}</code>
           <p className="note">Permanent QR: not transaction-specific and not retailer-specific.</p></section>
           </main>
}
