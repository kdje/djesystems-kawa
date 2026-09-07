# Authentication

The MVP uses Firebase Authentication.

## Web responsibilities
- registration
- login
- logout
- email verification
- password reset
- optional Google/social login
- obtain Firebase ID token

## API calls

Send the Firebase ID token as:

```http
Authorization: Bearer <firebase-id-token>
```

The backend verifies it with the Firebase Admin SDK.

KAWA never stores passwords.
