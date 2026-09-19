ALTER TABLE accounts
    RENAME CONSTRAINT unique_email TO accounts_email_unique;