import { notificationApi } from '../api/notifications'
import { firebaseApp, firebaseEnabled } from './firebase'

const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY

/**
 * Asks for notification permission and registers this browser for FCM web push.
 * The service worker gets the public Firebase config through its URL.
 */
export async function enablePushNotifications(): Promise<boolean> {
  if (!firebaseEnabled || !vapidKey || !('serviceWorker' in navigator) || !('Notification' in window)) return false
  if ((await Notification.requestPermission()) !== 'granted') return false

  const config = new URLSearchParams({
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID,
  })
  const registration = await navigator.serviceWorker.register(`/firebase-messaging-sw.js?${config}`)
  const [{ getMessaging, getToken }, app] = await Promise.all([import('firebase/messaging'), firebaseApp()])
  const token = await getToken(getMessaging(app), { vapidKey, serviceWorkerRegistration: registration })
  if (!token) return false
  await notificationApi.registerDevice(token)
  return true
}
