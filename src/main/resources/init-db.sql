-- init-db.sql
-- Script de inicialização do banco de dados para o Painel VPN

-- 1. Criação do banco de dados (se não existir)
DROP DATABASE IF EXISTS painel;

CREATE DATABASE IF NOT EXISTS painel
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE painel;

-- 2. Criação da tabela de usuários
CREATE TABLE IF NOT EXISTS vpn_users (
    user_id BINARY(16) NOT NULL,
    username VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    vpn_certificate_identifier VARCHAR(7) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    vpn_enabled BOOLEAN DEFAULT FALSE,
    certificate_id VARCHAR(255),
    certificate_expiry DATETIME,
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    login_attempts INT NOT NULL DEFAULT 0,
    account_non_locked BOOLEAN DEFAULT TRUE,
    lock_time DATETIME,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Criação da tabela de certificados VPN
CREATE TABLE IF NOT EXISTS vpn_certificates (
    id VARCHAR(36) NOT NULL,
    certificate_id VARCHAR(255) NOT NULL,
    user_id BINARY(16) NOT NULL,
    common_name VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL,
    fingerprint VARCHAR(255) NOT NULL,
    issue_date DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revocation_date DATETIME,
    revocation_reason VARCHAR(255),
    key_algorithm VARCHAR(50) NOT NULL,
    key_size INT NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    certificate_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_certificate_id (certificate_id),
    UNIQUE KEY uk_serial_number (serial_number),
    UNIQUE KEY uk_fingerprint (fingerprint),
    KEY idx_user_id (user_id),
    KEY idx_expiry_date (expiry_date),
    CONSTRAINT fk_cert_user FOREIGN KEY (user_id) REFERENCES vpn_users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Inserção do usuário administrador padrão
-- Senha: admin123 (hash BCrypt)
INSERT IGNORE INTO vpn_users (
    user_id,
    username,
    full_name,
    email,
    password,
    is_active,
    vpn_enabled,
    role
) VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'admin',
    'Administrador do Sistema',
    'admin@vpnmanager.com',
    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUa95gWklhrdoaBXE2W', -- admin123
    TRUE,
    TRUE,
    'ADMIN'
);

-- 5. Índices adicionais para melhorar desempenho
-- Nota: Removido IF NOT EXISTS pois não é suportado pelo MySQL para índices
CREATE INDEX idx_vpn_users_vpn_enabled ON vpn_users (vpn_enabled);
CREATE INDEX idx_vpn_users_is_active ON vpn_users (is_active);
CREATE INDEX idx_vpn_certificates_revoked ON vpn_certificates (revoked);