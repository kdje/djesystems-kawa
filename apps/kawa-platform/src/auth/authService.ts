import { Capacitor } from "@capacitor/core";
import {
  FirebaseAuthentication
} from "@capacitor-firebase/authentication";

import {
  GoogleAuthProvider,
  signInWithCredential,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut as firebaseSignOut,
  type User,
} from "firebase/auth";

import { auth } from "../firebase/firebase";


export async function loginWithEmailPassword(
  email: string,
  password: string
): Promise<User> {

  const credential =
    await signInWithEmailAndPassword(
      auth,
      email,
      password
    );

  return credential.user;
}


export async function loginWithGoogle():
  Promise<User> {

  /*
   * WEB
   */
  if (!Capacitor.isNativePlatform()) {

    const provider =
      new GoogleAuthProvider();

    const credential =
      await signInWithPopup(
        auth,
        provider
      );

    return credential.user;
  }


  /*
   * ANDROID / IOS
   *
   * 1. Authentification Google native
   */
  const nativeResult =
    await FirebaseAuthentication.signInWithGoogle();

  const idToken =
    nativeResult.credential?.idToken;

  if (!idToken) {
    throw new Error(
      "Google n'a retourné aucun ID token."
    );
  }


  /*
   * 2. Création d'un credential Firebase JS
   */
  const googleCredential =
    GoogleAuthProvider.credential(
      idToken
    );


  /*
   * 3. Connexion sur la couche Firebase JS
   *
   * Cela permet de conserver :
   * - useFirebaseUser()
   * - onAuthStateChanged()
   * - user.getIdToken()
   */
  const firebaseCredential =
    await signInWithCredential(
      auth,
      googleCredential
    );

  return firebaseCredential.user;
}


export async function logout():
  Promise<void> {

  if (Capacitor.isNativePlatform()) {

    await FirebaseAuthentication.signOut();

  }

  await firebaseSignOut(auth);
}


export async function getFirebaseIdToken(
  user: User
): Promise<string> {

  return user.getIdToken();
}