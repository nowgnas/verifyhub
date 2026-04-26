CREATE TABLE verification_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    verification_id VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    provider VARCHAR(20),
    status VARCHAR(30) NOT NULL,
    provider_transaction_id VARCHAR(100),
    provider_request_no VARCHAR(100),
    web_transaction_id VARCHAR(100),
    routing_policy_version BIGINT,
    requested_at DATETIME(6) NOT NULL,
    routed_at DATETIME(6),
    provider_called_at DATETIME(6),
    completed_at DATETIME(6),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_verification_id (verification_id),
    UNIQUE KEY uk_idempotency (request_id, purpose, idempotency_key),
    UNIQUE KEY uk_provider_transaction (provider, provider_transaction_id),
    UNIQUE KEY uk_provider_request_no (provider, provider_request_no),
    UNIQUE KEY uk_provider_web_transaction (provider, web_transaction_id),
    KEY idx_verification_status_created_at (status, created_at),
    KEY idx_verification_provider_created_at (provider, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE verification_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    verification_id VARCHAR(64) NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    reason VARCHAR(255),
    provider VARCHAR(20),
    raw_payload JSON,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_verification_history_01 (verification_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE provider_call_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    verification_id VARCHAR(64) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    request_payload JSON,
    response_payload JSON,
    http_status INT,
    result_type VARCHAR(30),
    latency_ms BIGINT,
    error_message VARCHAR(500),
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_provider_call_01 (verification_id),
    KEY idx_provider_call_02 (provider, created_at),
    KEY idx_provider_call_03 (result_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE late_callback_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    verification_id VARCHAR(64) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    current_status VARCHAR(30) NOT NULL,
    callback_result VARCHAR(30) NOT NULL,
    duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    raw_payload JSON,
    reason VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_late_callback_01 (verification_id, created_at),
    KEY idx_late_callback_02 (provider, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE provider_routing_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider VARCHAR(20) NOT NULL,
    weight INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_policy (provider, version),
    KEY idx_provider_policy_01 (enabled, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    published_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_outbox_01 (status, created_at),
    KEY idx_outbox_02 (aggregate_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
