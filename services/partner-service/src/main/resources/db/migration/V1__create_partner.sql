CREATE TABLE partner_authorization (id CHAR(36) PRIMARY KEY, oauth_client_id VARCHAR(255) NOT NULL, partner_name VARCHAR(180) NOT NULL, retailer_code VARCHAR(80), enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX ix_partner_client ON partner_authorization(oauth_client_id);
CREATE TABLE resolution_audit (id CHAR(36) PRIMARY KEY, partner_client_id VARCHAR(255) NOT NULL, retailer_code VARCHAR(80) NOT NULL, kawa_public_id_hash VARCHAR(128) NOT NULL, outcome VARCHAR(40) NOT NULL, occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
