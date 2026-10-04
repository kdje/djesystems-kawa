CREATE TABLE retailer_user (
    id CHAR(36) NOT NULL,
    retailer_id CHAR(36) NOT NULL,
    identity_provider VARCHAR(30) NOT NULL,
    identity_tenant_id VARCHAR(180) NOT NULL,
    identity_subject VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_retailer_user_retailer
        FOREIGN KEY (retailer_id) REFERENCES retailer(id),
    CONSTRAINT uk_retailer_user_identity_retailer
        UNIQUE (retailer_id, identity_provider, identity_tenant_id, identity_subject),
    INDEX ix_retailer_user_identity
        (identity_provider, identity_tenant_id, identity_subject)
);

CREATE TABLE retailer_subscription (
    id CHAR(36) NOT NULL,
    retailer_id CHAR(36) NOT NULL,
    plan VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    start_date DATE,
    renewal_date DATE,
    billing_cycle VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_retailer_subscription_retailer
        FOREIGN KEY (retailer_id) REFERENCES retailer(id),
    CONSTRAINT uk_retailer_subscription_retailer UNIQUE (retailer_id)
);

CREATE TABLE retailer_billing_account (
    id CHAR(36) NOT NULL,
    retailer_id CHAR(36) NOT NULL,
    company_name VARCHAR(180) NOT NULL,
    billing_email VARCHAR(320) NOT NULL,
    billing_address VARCHAR(1000),
    external_provider VARCHAR(100),
    external_account_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_retailer_billing_account_retailer
        FOREIGN KEY (retailer_id) REFERENCES retailer(id),
    CONSTRAINT uk_retailer_billing_account_retailer UNIQUE (retailer_id)
);
