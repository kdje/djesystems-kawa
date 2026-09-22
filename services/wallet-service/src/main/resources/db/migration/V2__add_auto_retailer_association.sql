ALTER TABLE customer_projection
    ADD COLUMN auto_retailer_association_enabled BOOLEAN NOT NULL DEFAULT FALSE
    AFTER status;

ALTER TABLE retailer_customer_mapping
    ADD COLUMN consent_mode VARCHAR(40) NULL AFTER status,
    ADD COLUMN consent_at TIMESTAMP(6) NULL AFTER consent_mode;
