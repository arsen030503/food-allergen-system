-- Schema aligned with JPA entities (Spring Boot default naming: camelCase -> snake_case)

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    my_allergens    TEXT,
    created_at      VARCHAR(255),
    avatar_data     TEXT,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE allergens (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    standard             VARCHAR(255) NOT NULL,
    trigger_ingredients  TEXT,
    severity             VARCHAR(50),
    description          TEXT
);

CREATE TABLE scan_history (
    id                   BIGSERIAL PRIMARY KEY,
    ingredients          TEXT NOT NULL,
    detected_allergens   TEXT,
    scanned_at           TIMESTAMP WITHOUT TIME ZONE,
    allergen_count       INTEGER NOT NULL DEFAULT 0,
    user_id              BIGINT,
    product_name         TEXT
);

CREATE INDEX idx_scan_history_user_id_scanned_at ON scan_history (user_id, scanned_at DESC);
