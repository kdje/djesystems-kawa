CREATE TABLE retailer (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    logo_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE outbox_event (id CHAR(36) PRIMARY KEY, aggregate_type VARCHAR(100) NOT NULL, aggregate_id VARCHAR(100) NOT NULL, event_type VARCHAR(150) NOT NULL, payload JSON NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, published_at TIMESTAMP);

CREATE TABLE retailer_credential (
    id CHAR(36) NOT NULL,

    retailer_id CHAR(36) NOT NULL,

    environment VARCHAR(20) NOT NULL,

    entra_client_id VARCHAR(36) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_retailer_credential_retailer
        FOREIGN KEY (retailer_id)
        REFERENCES retailer(id),

    CONSTRAINT uk_retailer_credential_environment
        UNIQUE (retailer_id, environment),

    CONSTRAINT uk_retailer_credential_client_id
        UNIQUE (entra_client_id)
);