import type { User } from "firebase/auth";
import { Capacitor } from "@capacitor/core";

import { getFirebaseIdToken } from "../auth/authService";
import type {
  Customer,
  KawaIdResponse,
} from "../types/Customer";

export type RetailerRelationStatus =
  | "PENDING"
  | "APPROVED"
  | "ACTIVE"
  | "REJECTED";

export type CustomerRetailerRelation = {
  retailerCode: string;
  status: RetailerRelationStatus;
  sourceEventId: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CustomerRetailersResponse = {
  activeCount: number;
  pendingCount: number;
  retailers: CustomerRetailerRelation[];
};

export type ConsentDecision =
  | "APPROVED"
  | "REJECTED";

const BASE_URL =
  import.meta.env.VITE_CUSTOMER_API_BASE_URL ??
  "http://localhost:8081";

/**
 * GET authentifié vers customer-service.
 */
async function getAuthenticated<T>(
  path: string,
  user: User
): Promise<T> {

  const token =
    await getFirebaseIdToken(user);

  const response = await fetch(
    `${BASE_URL}${path}`,
    {
      method: "GET",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    const responseBody =
      await response.text();

    throw new Error(
      `customer-service returned HTTP ${response.status}: ${responseBody}`
    );
  }

  return response.json() as Promise<T>;
}

/**
 * Retourne le customer KAWA connecté.
 */
export const getCurrentCustomer = (
  user: User
) =>
  getAuthenticated<Customer>(
    "/api/customers/me",
    user
  );

/**
 * Retourne le KawaId public du customer connecté.
 */
export const getCurrentKawaId = (
  user: User
) =>
  getAuthenticated<KawaIdResponse>(
    "/api/customers/me/kawa-id",
    user
  );

/**
 * Retourne les associations enseignes du customer connecté.
 */
export const getMyRetailers = (
  user: User
) =>
  getAuthenticated<CustomerRetailersResponse>(
    "/api/customers/me/retailers",
    user
  );

/**
 * Enregistre le device FCM du customer connecté.
 */
export async function registerNotificationDevice(
  user: User,
  fcmToken: string
): Promise<void> {

  const firebaseIdToken =
    await getFirebaseIdToken(user);

  const capacitorPlatform =
    Capacitor.getPlatform();

  const platform =
    capacitorPlatform === "android"
      ? "ANDROID"
      : capacitorPlatform === "ios"
        ? "IOS"
        : "WEB";

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
        platform,
      }),
    }
  );

  if (!response.ok) {
    const responseBody =
      await response.text();

    throw new Error(
      `customer-service returned HTTP ${response.status}: ${responseBody}`
    );
  }
}

/**
 * Accepte ou refuse une demande d'association enseigne.
 */
export async function respondToConsentRequest(
  user: User,
  eventId: string,
  decision: ConsentDecision
): Promise<void> {

  const firebaseIdToken =
    await getFirebaseIdToken(user);

  const response = await fetch(
    `${BASE_URL}/api/customers/me/consent-requests/${encodeURIComponent(
      eventId
    )}/decision`,
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
    const responseBody =
      await response.text();

    throw new Error(
      `Impossible d'enregistrer le consentement : HTTP ${response.status}: ${responseBody}`
    );
  }
}
/**
 * Active/désactive l'association automatique lors de la présentation du QR code.
 */
export async function updateAutoRetailerAssociation(
  user: User,
  enabled: boolean
): Promise<Customer> {

  const firebaseIdToken =
    await getFirebaseIdToken(user);

  const response = await fetch(
    `${BASE_URL}/api/customers/me/preferences`,
    {
      method: "PATCH",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${firebaseIdToken}`,
      },
      body: JSON.stringify({
        autoRetailerAssociationEnabled: enabled,
      }),
    }
  );

  if (!response.ok) {
    const responseBody = await response.text();
    throw new Error(
      `Impossible de modifier la préférence : HTTP ${response.status}: ${responseBody}`
    );
  }

  return response.json() as Promise<Customer>;
}
