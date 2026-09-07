CREATE TABLE loyalty_account (id CHAR(36) PRIMARY KEY, customer_internal_id CHAR(36) NOT NULL, retailer_id CHAR(36) NOT NULL, retailer_code VARCHAR(80) NOT NULL, loyalty_identifier VARCHAR(255) NOT NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT uq_customer_retailer UNIQUE (customer_internal_id, retailer_id));
CREATE INDEX ix_loyalty_customer ON loyalty_account(customer_internal_id);

CREATE TABLE retailer_customer_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,

    public_kawa_id VARCHAR(100) NOT NULL,

    retailer_code VARCHAR(100) NOT NULL,

    retailer_customer_id VARCHAR(255) NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_wallet_public_kawa_retailer
        UNIQUE (public_kawa_id, retailer_code),

    CONSTRAINT uk_wallet_retailer_customer
        UNIQUE (retailer_code, retailer_customer_id)
);

CREATE TABLE customer_projection (
    public_kawa_id VARCHAR(100) NOT NULL,
    email VARCHAR(320) NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (public_kawa_id)
);

CREATE TABLE retailer_projection (
    retailer_id CHAR(36) NOT NULL,
    retailer_code VARCHAR(80) NOT NULL,
    name VARCHAR(180) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (retailer_id),

    CONSTRAINT uk_wallet_retailer_code
        UNIQUE (retailer_code)
);


CREATE TABLE retailer_credential_projection (
    id BIGINT NOT NULL AUTO_INCREMENT,

    retailer_id CHAR(36) NOT NULL,

    retailer_code VARCHAR(80) NOT NULL,

    environment VARCHAR(20) NOT NULL,

    entra_client_id VARCHAR(36) NOT NULL,

    enabled BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_wallet_retailer_credential_environment
        UNIQUE (retailer_id, environment),

    CONSTRAINT uk_wallet_retailer_entra_client_id
        UNIQUE (entra_client_id)
);

CREATE TABLE outbox_event (

    event_id VARCHAR(36) NOT NULL,

    aggregate_type VARCHAR(100) NOT NULL,

    aggregate_id VARCHAR(100) NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    payload TEXT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    processing_started_at TIMESTAMP(6) NULL,

    published_at TIMESTAMP(6) NULL,

    attempt_count INT NOT NULL DEFAULT 0,

    last_error VARCHAR(2000) NULL,

    PRIMARY KEY (event_id),

    INDEX idx_outbox_pending (
        status,
        created_at
    ),

    INDEX idx_outbox_processing (
        status,
        processing_started_at
    )
);