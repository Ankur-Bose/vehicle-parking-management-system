DROP DATABASE IF EXISTS slot_service_db;
CREATE DATABASE slot_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE slot_service_db;
CREATE TABLE parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(5) NOT NULL,
    location VARCHAR(50) NOT NULL,
    is_occupied BOOLEAN NOT NULL DEFAULT FALSE,
    is_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO parking_slots (type, location, is_occupied, is_disabled) VALUES
  -- 2-Wheeler slots
  ('2W', 'A-A1', FALSE, FALSE),   
  ('2W', 'A-A2', FALSE, FALSE),   
  ('2W', 'A-A3', TRUE,  FALSE),   
  ('2W', 'A-A4', FALSE, FALSE),   
  ('2W', 'A-A5', FALSE, TRUE),    

  -- 4-Wheeler slots
  ('4W', 'G-B1', FALSE, FALSE),   
  ('4W', 'G-B2', FALSE, FALSE),   
  ('4W', 'G-B3', TRUE,  FALSE),   
  ('4W', 'G-B4', FALSE, FALSE),   
  ('4W', 'G-B5', FALSE, FALSE);

SELECT 
    *
FROM
    parking_slots;
SELECT 
    type,
    COUNT(*) AS total,
    SUM(is_occupied = FALSE
        AND is_disabled = FALSE) AS available,
    SUM(is_occupied = TRUE) AS occupied,
    SUM(is_disabled = TRUE) AS disabled
FROM
    parking_slots
GROUP BY type;
