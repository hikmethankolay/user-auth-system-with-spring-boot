-- Drop the schema if it already exists (use with caution in production)
DROP SCHEMA IF EXISTS user_auth_db;

-- Create the schema (database)
CREATE SCHEMA user_auth_db;

-- Switch to the newly created schema
USE user_auth_db;

---------------------------------------------------
-- Create the users table
---------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uc_username UNIQUE (username),
    CONSTRAINT uc_email UNIQUE (email)
);

---------------------------------------------------
-- Create the roles table
---------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    CONSTRAINT uc_role_name UNIQUE (name)
);

---------------------------------------------------
-- Create the user_roles joint table
---------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

---------------------------------------------------
-- Insert roles ro roles table
---------------------------------------------------
INSERT INTO roles (name) 
VALUES ('ROLE_ADMIN'), 
       ('ROLE_MODERATOR'), 
       ('ROLE_USER');

