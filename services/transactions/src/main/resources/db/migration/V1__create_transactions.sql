CREATE TABLE transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key  VARCHAR(64),
    from_account     UUID NOT NULL,
    to_account       UUID NOT NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  UUID NOT NULL REFERENCES transactions(id),
    account_id      UUID NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    amount          NUMERIC(19, 4) NOT NULL
);
