export type Customer = {
  publicKawaId: string;
  email: string | null;
  status: "ACTIVE" | "SUSPENDED" | "DELETED";
  autoRetailerAssociationEnabled: boolean;
  createdAt: string;
};

export type KawaIdResponse = {
  kawaId: string;
};
