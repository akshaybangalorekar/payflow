CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name   VARCHAR(120)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    balance         DOUBLE PRECISION NOT NULL DEFAULT 0,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    internal_notes  TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
