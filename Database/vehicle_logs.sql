DROP DATABASE IF EXISTS vehicle_log_service_db;
CREATE DATABASE vehicle_log_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vehicle_log_service_db;

CREATE TABLE vehicle_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vehicle_number (vehicle_number),
    INDEX idx_user_id (user_id),
    INDEX idx_slot_id (slot_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_entry_time (entry_time)
);

INSERT INTO vehicle_logs (user_id, slot_id, reservation_id, vehicle_number, entry_time, exit_time) VALUES
  (3, 3, 1, 'TN01AB1234', '2026-04-20 10:00:00', NULL),

  (2, 1, 2, 'MH02CD5678', '2026-04-18 08:00:00', '2026-04-18 11:00:00');

SELECT 
    *
FROM
    vehicle_logs;
SELECT 
    *
FROM
    vehicle_logs
WHERE
    exit_time IS NULL;