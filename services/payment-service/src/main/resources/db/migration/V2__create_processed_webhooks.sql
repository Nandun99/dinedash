-- Stripe retries webhooks; remembering processed event ids makes handling idempotent.
CREATE TABLE processed_webhook_events (
    event_id     VARCHAR2(100) PRIMARY KEY,
    event_type   VARCHAR2(100) NOT NULL,
    processed_at TIMESTAMP     NOT NULL
);
