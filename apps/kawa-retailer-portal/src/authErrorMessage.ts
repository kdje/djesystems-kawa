const knownMessages: Record<string, string> = {
  "auth/unauthorized-domain":
    "Ce domaine n’est pas autorisé pour la connexion Firebase. Demandez à l’administrateur d’ajouter le domaine du portail aux domaines autorisés.",
  "auth/operation-not-allowed":
    "La connexion Google n’est pas activée pour ce projet Firebase. Contactez l’administrateur.",
  "auth/popup-blocked":
    "La fenêtre de connexion Google a été bloquée par le navigateur. Autorisez les fenêtres pop-up pour ce site puis réessayez.",
  "auth/popup-closed-by-user":
    "La fenêtre de connexion Google a été fermée avant la fin de la connexion. Réessayez.",
  "auth/network-request-failed":
    "La connexion au service Firebase a échoué. Vérifiez votre connexion réseau puis réessayez.",
  "auth/invalid-api-key":
    "La configuration Firebase du portail est invalide. Contactez l’administrateur.",
  "auth/api-key-not-valid":
    "La clé API Firebase du portail n’est pas valide pour ce domaine. Contactez l’administrateur.",
  "auth/wrong-password":
    "Adresse e-mail ou mot de passe incorrect.",
  "auth/user-not-found":
    "Adresse e-mail ou mot de passe incorrect.",
  "auth/invalid-email":
    "Adresse e-mail ou mot de passe incorrect.",
  "auth/too-many-requests":
    "Trop de tentatives de connexion. Patientez quelques minutes puis réessayez.",
};

export function authErrorMessage(error: unknown): string {
  const code =
    typeof error === "object" &&
    error !== null &&
    "code" in error &&
    typeof error.code === "string"
      ? error.code
      : undefined;

  if (code && knownMessages[code]) {
    return knownMessages[code];
  }

  const safeCode = code && /^auth\/[a-z0-9-]+$/.test(code) ? code : undefined;
  return safeCode
    ? `La connexion a échoué. Réessayez ou contactez l’administrateur avec ce code Firebase : ${safeCode}.`
    : "La connexion a échoué. Vérifiez vos identifiants ou réessayez.";
}
