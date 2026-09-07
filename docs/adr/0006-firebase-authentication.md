# ADR 0006 — Firebase Authentication for consumer accounts

## Decision
KAWA uses Firebase Authentication for the MVP.

## Responsibilities delegated to Firebase
- user registration
- login
- password reset
- email verification
- social login if enabled
- issuance of Firebase ID tokens

## Backend integration
React obtains a Firebase ID token and sends it as a Bearer token.
Each Java API validates the token using Firebase Admin SDK.

KAWA stores the Firebase `uid` as an external identity reference.
KAWA never stores passwords.
