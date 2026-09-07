export type Customer = {
  publicKawaId: string;
  email: string | null;
  status: "ACTIVE" | "SUSPENDED" | "DELETED";
  createdAt: string;
};

export type KawaIdResponse = {
  kawaId: string;
};
