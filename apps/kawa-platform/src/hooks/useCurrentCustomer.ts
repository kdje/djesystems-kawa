import { useEffect, useState } from "react";
import type { User } from "firebase/auth";
import { getCurrentCustomer } from "../api/customerApi";
import type { Customer } from "../types/Customer";

export function useCurrentCustomer(user: User | null) {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [customerLoading, setCustomerLoading] = useState(false);
  const [customerError, setCustomerError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) {
      setCustomer(null);
      setCustomerError(null);
      return;
    }

    let cancelled = false;
    setCustomerLoading(true);
    setCustomerError(null);

    getCurrentCustomer(user)
      .then((value) => !cancelled && setCustomer(value))
      .catch((error: unknown) => {
        if (!cancelled) {
          setCustomerError(error instanceof Error ? error.message : "Unknown error");
        }
      })
      .finally(() => !cancelled && setCustomerLoading(false));

    return () => { cancelled = true; };
  }, [user]);

  return { customer, customerLoading, customerError };
}
