import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { notificationApi, type AppNotification } from '../api/notifications'
import { firebaseEnabled } from '../lib/firebase'
import { enablePushNotifications } from '../lib/push'
import { formatDateTime } from '../utils/format'

export default function NotificationBell() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const panel = useRef<HTMLDivElement>(null)

  const unread = useQuery({ queryKey: ['notifications', 'unread'], queryFn: notificationApi.unreadCount, refetchInterval: 30_000 })
  const list = useQuery({ queryKey: ['notifications', 'list'], queryFn: notificationApi.latest, enabled: open })
  const refresh = () => qc.invalidateQueries({ queryKey: ['notifications'] })
  const markAll = useMutation({ mutationFn: notificationApi.markAllRead, onSuccess: refresh })

  useEffect(() => {
    const close = (e: MouseEvent) => {
      if (panel.current && !panel.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [])

  const openItem = async (n: AppNotification) => {
    if (!n.read) await notificationApi.markRead(n.id)
    refresh()
    setOpen(false)
    if (n.link) navigate(n.link)
  }

  const count = unread.data ?? 0
  return (
    <div className="relative" ref={panel}>
      <button onClick={() => setOpen((v) => !v)} className="relative rounded-full p-2 hover:bg-stone-100" aria-label="Notifications">
        <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9M13.7 21a2 2 0 0 1-3.4 0" />
        </svg>
        {count > 0 && (
          <span className="absolute -right-0.5 -top-0.5 rounded-full bg-brand-600 px-1.5 text-[10px] font-bold text-white">{count > 9 ? '9+' : count}</span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 z-[1000] mt-2 w-80 overflow-hidden rounded-2xl bg-white shadow-lg ring-1 ring-stone-200">
          <div className="flex items-center justify-between border-b border-stone-100 px-4 py-2 text-sm">
            <span className="font-semibold">Notifications</span>
            {count > 0 && <button onClick={() => markAll.mutate()} className="text-xs text-brand-600 hover:underline">Mark all read</button>}
          </div>
          <ul className="max-h-96 overflow-y-auto">
            {list.data?.length === 0 && <li className="p-4 text-sm text-stone-500">Nothing yet.</li>}
            {list.data?.map((n) => (
              <li key={n.id}>
                <button onClick={() => openItem(n)} className={`block w-full px-4 py-3 text-left text-sm hover:bg-stone-50 ${n.read ? '' : 'bg-brand-50/60'}`}>
                  <p className="font-medium">{n.title}</p>
                  <p className="text-stone-600">{n.body}</p>
                  <p className="mt-1 text-xs text-stone-400">{formatDateTime(n.createdAt)}</p>
                </button>
              </li>
            ))}
          </ul>
          {firebaseEnabled && typeof Notification !== 'undefined' && Notification.permission === 'default' && (
            <button onClick={() => enablePushNotifications()} className="w-full border-t border-stone-100 px-4 py-2 text-xs text-brand-600 hover:bg-stone-50">
              Turn on browser notifications
            </button>
          )}
        </div>
      )}
    </div>
  )
}
