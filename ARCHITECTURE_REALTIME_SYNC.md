# Real-Time Sync Architecture: SMA Website <-> SeyMeteo Android

To transform SeyMeteo into a **real-time event-driven weather platform**, we need coordinated architecture between the **SMA Backend Server** and the **Android App**.

```
┌─────────────────────────────────────────────────────────────┐
│                 SMA CMS / Backend Server                     │
│  (Django / Wagtail / FastAPI + PostgreSQL / PostGIS)         │
└──────┬───────────────────────┬───────────────────────┬──────┘
       │ Model Post-Save Hook  │ WMS GeoServer Ingest  │ CAP Bulletin Published
       │                       │                       │
┌──────▼───────────────────────▼───────────────────────▼──────┐
│                    Event & Dispatch Layer                   │
│   • Redis Pub/Sub                                           │
│   • Firebase Cloud Messaging (FCM) Topic Broadcaster        │
│   • Server-Sent Events (SSE) Stream                         │
│   • HTTP ETag / Cache-Control Invalidation                  │
└──────┬───────────────────────┬───────────────────────┬──────┘
       │ Push: Silent Data Msg │ Stream: EventSource   │ Conditional GET 304
       │                       │                       │
┌──────▼───────────────────────▼───────────────────────▼──────┐
│                  SeyMeteo Android Client                    │
│   • FirebaseMessagingService (Background Wakeup)            │
│   • OkHttp ETag Cache Interceptor (Zero-byte unchanged)     │
│   • WorkManager Exponential Backoff + Live Sync Engine      │
│   • Room DB Reactive Invalidation -> Compose UI StateFlow   │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. What the SMA Website / Backend Needs

The SMA platform (`meteo.sc`) currently runs a Django-based CMS with GeoServer for maps. Here are the 3 key capabilities that make the backend reactive:

### A. Instant Push Dispatch via Firebase Cloud Messaging (FCM)
When meteorologists publish a new bulletin, forecast, or CAP alert in the Django admin:
- **Trigger**: Django `post_save` signal on the `Forecast` and `CapAlert` models.
- **Action**: Server dispatches an FCM **High-Priority Data Message** to public topics:
  - `topics/forecast_updates` (Payload: `{"type": "FORECAST_UPDATED", "timestamp": "...", "island": "all"}`)
  - `topics/cap_alerts_emergency` (Payload: `{"type": "CAP_ALERT", "severity": "Extreme", "title": "Cyclone Warning"}`)
- **Benefit**: Android wakes up instantly in 500ms without wasting battery polling.

```python
# Sample Django Signal (Backend)
from django.db.models.signals import post_save
from django.dispatch import receiver
from firebase_admin import messaging

@receiver(post_save, sender=DailyForecast)
def notify_forecast_updated(sender, instance, created, **kwargs):
    message = messaging.Message(
        data={
            'event': 'FORECAST_REFRESH',
            'updated_at': instance.updated_at.isoformat(),
            'island_slug': instance.city.slug,
        },
        topic='forecast_updates'
    )
    messaging.send(message)
```

### B. HTTP Conditional GETs (`ETag` and `Last-Modified`)
Currently, polling fetches the full JSON payload every time even if data hasn't changed.
- The server should attach `ETag: "w/1234abcd"` and `Last-Modified: Sat, 05 Sep 2026 11:30:00 GMT` to all `/weather/*` and `/api/*` endpoints.
- If no update occurred, the server returns a lightweight **`HTTP 304 Not Modified`** (0 bytes payload, saving massive bandwidth on outer island 3G/4G).

### C. Server-Sent Events (SSE) `/api/stream/live`
For users with the app actively open in foreground:
- An open HTTP streaming connection (`GET /api/stream/live`) that pushes JSON diffs when radar frames update (every 15 min) or tide points change.

---

## 2. What We Wire in the SeyMeteo Android App

To consume and act on these server signals, the Android app implements an **Event-Driven Repository**:

### A. FCM Wakeup Service (`SeyMeteoMessagingService`)
Listens for silent server signals in the background:
- When a `FORECAST_REFRESH` message arrives:
  1. Wakes up the app's background worker via `OneTimeWorkRequestBuilder<ForecastSyncWorker>()`.
  2. Fetches the delta payload and writes to **Room Database**.
  3. Because UI components observe Room via `Flow<T>`, the UI automatically re-renders without the user needing to touch refresh.

### B. Smart ETag OkHttp Cache Interceptor
- Adds an `OkHttp Cache` directory with `If-None-Match` header injection.
- Seamlessly handles `304 Not Modified` responses so repeated background syncs consume zero unnecessary battery or mobile data.

### C. Broadcast Receiver for Connectivity & Resiliency
- Listens to Android system network transitions (e.g. phone regaining Wi-Fi/4G after leaving a signal dead-zone) and triggers immediate reconciliation.
