ALTER TABLE accounts
    ADD CONSTRAINT unique_email UNIQUE (email);