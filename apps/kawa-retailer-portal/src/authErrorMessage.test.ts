import { describe, expect, it } from "vitest";
import { authErrorMessage } from "./authErrorMessage";

describe("authErrorMessage", () => {
  it.each([
    ["auth/unauthorized-domain", "domaine n’est pas autorisé"],
    ["auth/operation-not-allowed", "n’est pas activée"],
    ["auth/popup-blocked", "a été bloquée"],
    ["auth/popup-closed-by-user", "a été fermée"],
    ["auth/network-request-failed", "connexion au service Firebase"],
    ["auth/invalid-api-key", "configuration Firebase"],
  ])("explains Firebase error %s", (code, expectedText) => {
    expect(authErrorMessage({ code })).toContain(expectedText);
  });

  it("keeps credential failures distinct from provider configuration failures", () => {
    expect(authErrorMessage({ code: "auth/wrong-password" })).toContain(
      "Adresse e-mail ou mot de passe incorrect",
    );
    expect(authErrorMessage({ code: "auth/wrong-password" })).not.toContain(
      "Google",
    );
  });

  it("keeps invalid credentials neutral because the code can come from Google sign-in", () => {
    const message = authErrorMessage({ code: "auth/invalid-credential" });

    expect(message).toContain("auth/invalid-credential");
    expect(message).toContain("Réessayez ou contactez l’administrateur");
    expect(message).not.toContain("mot de passe");
    expect(message).not.toContain("Adresse e-mail");
  });

  it("shows a safe Firebase code for unrecognized Firebase errors", () => {
    expect(authErrorMessage({ code: "auth/internal-error" })).toContain(
      "auth/internal-error",
    );
    expect(authErrorMessage({ code: "auth/internal-error" })).not.toContain(
      "token",
    );
  });

  it("does not expose arbitrary error data", () => {
    expect(authErrorMessage(new Error("private error details"))).toBe(
      "La connexion a échoué. Vérifiez vos identifiants ou réessayez.",
    );
  });
});
