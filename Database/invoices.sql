DROP DATABASE IF EXISTS billing_service_db;
CREATE DATABASE billing_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE billing_service_db;

CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    log_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    amount DECIMAL(10 , 2 ) NOT NULL DEFAULT 0.00,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_status (status),
    INDEX idx_generated_at (generated_at)
);

CREATE TABLE pricing_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type VARCHAR(5) NOT NULL UNIQUE,
    rate_per_hr DECIMAL(8 , 2 ) NOT NULL,
    min_charge DECIMAL(8 , 2 ) NOT NULL DEFAULT 0.00,
    effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO pricing_rules (vehicle_type, rate_per_hr, min_charge) VALUES
  ('2W', 30.00, 30.00),
  ('4W', 60.00, 60.00);
INSERT INTO invoices (user_id, reservation_id, log_id, slot_id, amount, status, payment_method, generated_at, paid_at) VALUES
  (2, 2, 2, 1, 150.00, 'PAID', 'UPI', '2026-04-18 11:05:00', '2026-04-18 11:10:00');

SELECT * FROM invoices;
SELECT * FROM pricing_rules;
SELECT 
    status, COUNT(*) AS count, SUM(amount) AS total
FROM
    invoices
GROUP BY status;