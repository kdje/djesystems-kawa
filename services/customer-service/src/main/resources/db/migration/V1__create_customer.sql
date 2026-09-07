CREATE TABLE outbox_event (
    id CHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    payload JSON NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


CREATE TABLE customers (
    id BINARY(16) NOT NULL,
    firebase_uid VARCHAR(128) NOT NULL,
    public_kawa_id VARCHAR(32) NOT NULL,
    email VARCHAR(320) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_customer_firebase_uid
        UNIQUE (firebase_uid),

    CONSTRAINT uk_customer_public_kawa_id
        UNIQUE (public_kawa_id),

    CONSTRAINT chk_customer_status
        CHECK (status IN (
            'ACTIVE',
            'SUSPENDED',
            'DELETED'
        ))
)
ENGINE = InnoDB
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


CREATE TABLE customer_consent_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(36) NOT NULL,
    public_kawa_id VARCHAR(32) NOT NULL,
    retailer_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP(6) NOT NULL,
    responded_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_consent_event_id
        UNIQUE (event_id),

    CONSTRAINT chk_consent_request_status
        CHECK (status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'EXPIRED'
        ))
)
ENGINE = InnoDB
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


CREATE TABLE notification_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(36) NOT NULL,
    public_kawa_id VARCHAR(32) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    payload JSON NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sent_at TIMESTAMP(6) NULL,
    retry_count INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_notification_event
        UNIQUE (event_id),

    CONSTRAINT chk_notification_outbox_status
        CHECK (status IN (
            'PENDING',
            'SENT',
            'FAILED'
        ))
)
ENGINE = InnoDB
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE TABLE customer_notification_device (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_kawa_id VARCHAR(32) NOT NULL,
    fcm_token VARCHAR(512)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,
    platform VARCHAR(20) NOT NULL DEFAULT 'WEB',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_customer_notification_device_token
        UNIQUE (fcm_token),

    CONSTRAINT fk_notification_device_customer
        FOREIGN KEY (public_kawa_id)
        REFERENCES customers(public_kawa_id)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;


CREATE INDEX idx_notification_device_customer
    ON customer_notification_device(public_kawa_id, active);

CREATE INDEX idx_consent_public_kawa_id
    ON customer_consent_request(public_kawa_id);

CREATE INDEX idx_consent_status
    ON customer_consent_request(status);

CREATE INDEX idx_notification_public_kawa_id
    ON notification_outbox(public_kawa_id);

CREATE INDEX idx_notification_status
    ON notification_outbox(status);