DROP DATABASE IF EXISTS reservation_service_db;
CREATE DATABASE reservation_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE reservation_service_db;

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO reservations (user_id, slot_id, vehicle_number, start_time, end_time, status) VALUES
  (3, 3, 'TN01AB1234', '2026-04-20 10:00:00', NULL, 'ACTIVE'),
  (2, 1, 'MH02CD5678', '2026-04-18 08:00:00', '2026-04-18 11:00:00', 'COMPLETED');

SELECT * FROM reservations;