-- V2__create_users.sql
-- Creates the users table for P01 v2's Login/Session feature.

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- Seed user for testing (password: "Password123!" — hashed with bcrypt below)
-- NOTE: actual hash generation happens in Sprint 2 when we write the LoginServlet.
-- Placeholder row inserted here; real bcrypt hash substituted before Sprint 2 testing.
INSERT INTO users (username, password_hash)
VALUES ('testuser', '$2a$10$ccmg6mcdzL9M0fCz0M62y./g6yRD4qCRYlnpjBeXCFMND9SIRG/k.');