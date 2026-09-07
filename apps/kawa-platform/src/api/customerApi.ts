import type { User } from "firebase/auth";
import { getFirebaseIdToken } from "../auth/authService";
import type { Customer, KawaIdResponse } from "../types/Customer";

const BASE_URL =
  import.meta.env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8081";

async function getAuthenticated<T>(
  path: string,
  user: User
): Promise<T> {
  const token = await getFirebaseIdToken(user);

  const response = await fetch(`${BASE_URL}${path}`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(
      `customer-service returned HTTP ${response.status}: ${await response.text()}`
    );
  }

  return response.json() as Promise<T>;
}

export const getCurrentCustomer = (user: User) =>
  getAuthenticated<Customer>("/api/customers/me", user);

export const getCurrentKawaId = (user: User) =>
  getAuthenticated<KawaIdResponse>(
    "/api/customers/me/kawa-id",
    user
  );

export async function registerNotificationDevice(
  user: User,
  fcmToken: string
): Promise<void> {

  const firebaseIdToken =
    await getFirebaseIdToken(user);

  const response = await fetch(
    `${BASE_URL}/api/customers/me/notification-devices`,
    {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${firebaseIdToken}`,
      },
      body: JSON.stringify({
        token: fcmToken,
        platform: "WEB",
      }),
    }
  );

  if (!response.ok) {
    throw new Error(
      `customer-service returned HTTP ${response.status}: ${await response.text()}`
    );
  }
}

export type ConsentDecision =
  "APPROVED" | "REJECTED";

export async function respondToConsentRequest(
  user: User,
  eventId: string,
  decision: ConsentDecision
): Promise<void> {

  const firebaseIdToken =
    await getFirebaseIdToken(user);

  const response = await fetch(
    `${BASE_URL}/api/customers/me/consent-requests/${encodeURIComponent(eventId)}/decision`,
    {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${firebaseIdToken}`,
      },
      body: JSON.stringify({
        decision,
      }),
    }
  );

  if (!response.ok) {
    throw new Error(
      `Impossible d'enregistrer le consentement : HTTP ${response.status}: ${await response.text()}`
    );
  }
}