DROP DATABASE IF EXISTS user_service_db;
CREATE DATABASE user_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE user_service_db;

CREATE TABLE users (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    phone_number  VARCHAR(15)   NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER', 
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO users (name, email, phone_number, password_hash, role) VALUES
  ('Admin User', 'admin@vpms.com', '9000000001',
   '$2a$10$7QJ8Z1z1z1z1z1z1z1z1zuXwQ1z1z1z1z1z1z1z1z1z1z1z1z1zu', 'ADMIN'),
  ('John Doe',   'john@vpms.com',  '9000000002',
   '$2a$10$7QJ8Z1z1z1z1z1z1z1z1zuXwQ1z1z1z1z1z1z1z1z1z1z1z1z1zu', 'CUSTOMER'),
  ('Ravi Kumar', 'ravi@vpms.com',  '9000000003',
   '$2a$10$7QJ8Z1z1z1z1z1z1z1z1zuXwQ1z1z1z1z1z1z1z1z1z1z1z1z1zu', 'CUSTOMER');
   
   
SELECT * FROM users;