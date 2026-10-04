export type RetailerRole =
  | "RETAILER_ADMIN"
  | "BILLING_ADMIN"
  | "OPERATOR"
  | "VIEWER";

export interface PortalRetailer {
  code: string;
  name: string;
  countryCode: string;
  role: RetailerRole;
}

export interface PortalSubscription {
  plan: string;
  status: string;
  startDate: string | null;
  renewalDate: string | null;
  billingCycle: string | null;
}

export interface PortalBilling {
  companyName: string;
  billingEmail: string;
  billingAddress: string | null;
  externalProvider: string | null;
  externalAccountId: string | null;
}
