ALTER TABLE customers
    ADD COLUMN auto_retailer_association_enabled BOOLEAN NOT NULL DEFAULT FALSE
    AFTER status;
