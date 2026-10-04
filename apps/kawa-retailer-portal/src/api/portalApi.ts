import type { PortalBilling, PortalRetailer, PortalSubscription } from "../types";

export class PortalApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = "PortalApiError";
  }
}

type TokenProvider = () => Promise<string | null>;
type FetchFunction = typeof fetch;

export function createPortalApi(
  baseUrl: string,
  getToken: TokenProvider,
  fetcher: FetchFunction = fetch,
) {
  async function get<T>(path: string): Promise<T> {
    const token = await getToken();
    if (!token) {
      throw new PortalApiError("Votre session a expiré. Reconnectez-vous.", 401);
    }
    const response = await fetcher(`${baseUrl}${path}`, {
      method: "GET",
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!response.ok) {
      const payload = (await response.json().catch(() => null)) as
        | { message?: string }
        | null;
      throw new PortalApiError(
        payload?.message ?? "Une erreur est survenue lors du chargement.",
        response.status,
      );
    }
    return (await response.json()) as T;
  }

  return {
    getRetailer: () => get<PortalRetailer>("/api/retailer-portal/me"),
    getSubscription: () =>
      get<PortalSubscription>("/api/retailer-portal/me/subscription"),
    getBilling: () => get<PortalBilling>("/api/retailer-portal/me/billing"),
  };
}
