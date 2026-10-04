import { describe, expect, it, vi } from "vitest";
import { createPortalApi, PortalApiError } from "./portalApi";

describe("retailer portal API", () => {
  it("uses only the authenticated /me endpoints and attaches the Firebase ID token", async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: "retailer-a", name: "Enseigne A" }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    const api = createPortalApi("https://api.example.test", async () => "firebase-id-token", fetcher);

    await api.getRetailer();

    expect(fetcher).toHaveBeenCalledWith(
      "https://api.example.test/api/retailer-portal/me",
      expect.objectContaining({
        headers: { Authorization: "Bearer firebase-id-token" },
      }),
    );
    expect(fetcher.mock.calls[0][0]).not.toContain("retailer-a");
  });

  it("does not make an API request without an authenticated token", async () => {
    const fetcher = vi.fn();
    const api = createPortalApi("", async () => null, fetcher);

    await expect(api.getBilling()).rejects.toMatchObject({
      status: 401,
    } satisfies Partial<PortalApiError>);
    expect(fetcher).not.toHaveBeenCalled();
  });
});
