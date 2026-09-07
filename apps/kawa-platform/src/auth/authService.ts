import {
          GoogleAuthProvider,
          User,
          signInWithEmailAndPassword,
          signInWithPopup,
          signOut
       } from "firebase/auth";

import { auth } from "../firebase/firebase";

export async function loginWithEmailPassword(email: string, password: string): Promise<User> {
  const credential = await signInWithEmailAndPassword(auth, email, password);
  return credential.user;
}

export async function loginWithGoogle(): Promise<User> {
  const provider = new GoogleAuthProvider();

  const credential = await signInWithPopup(auth, provider);

  return credential.user;
}

export async function logout(): Promise<void> {
  await signOut(auth);
}

export async function getFirebaseIdToken(user: User): Promise<string> {
  return user.getIdToken();
}
