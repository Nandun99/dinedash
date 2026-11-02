import type { FirebaseApp } from 'firebase/app'

const config = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  databaseURL: import.meta.env.VITE_FIREBASE_DATABASE_URL,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

export const firebaseEnabled = Boolean(config.apiKey && config.databaseURL)

let app: Promise<FirebaseApp> | null = null

/** Loaded on demand so pages that do not need Firebase stay small. */
export function firebaseApp(): Promise<FirebaseApp> {
  app ??= import('firebase/app').then(({ initializeApp }) => initializeApp(config))
  return app
}
