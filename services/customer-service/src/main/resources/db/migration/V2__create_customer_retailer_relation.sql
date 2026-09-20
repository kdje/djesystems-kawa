CREATE TABLE customer_retailer_relation (
    id BIGINT NOT NULL AUTO_INCREMENT,

    public_kawa_id VARCHAR(32) NOT NULL,
    retailer_code VARCHAR(64) NOT NULL,

    status VARCHAR(32) NOT NULL,

    source_event_id VARCHAR(36) NULL,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_customer_retailer_relation
        UNIQUE (public_kawa_id, retailer_code)
);

CREATE INDEX idx_customer_retailer_relation_customer
    ON customer_retailer_relation(public_kawa_id);

CREATE INDEX idx_customer_retailer_relation_status
    ON customer_retailer_relation(public_kawa_id, status);